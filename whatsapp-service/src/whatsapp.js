import makeWASocket, {
  useMultiFileAuthState,
  DisconnectReason,
  fetchLatestBaileysVersion
} from '@whiskeysockets/baileys';
import { Boom } from '@hapi/boom';
import pino from 'pino';
import path from 'path';
import fs from 'fs';
import qrcode from 'qrcode';
import axios from 'axios';

// ── Estado en memoria ─────────────────────────────────────────────────────────
const sessions  = new Map(); // sessionId → socket
const qrCodes   = new Map(); // sessionId → qr base64
const statusMap = new Map(); // sessionId → string

const SESSION_DIR  = process.env.SESSION_DIR  || './sessions';
const SPRING_URL   = process.env.SPRING_BOOT_URL || 'http://localhost:9090';

// ── Helpers internos ──────────────────────────────────────────────────────────

/**
 * Devuelve el socket activo de una sesión.
 * Lanza error si no existe o no está conectada.
 */
function getSocket(sessionId) {
  const sock = sessions.get(sessionId);
  if (!sock)
    throw new Error(`Sesión '${sessionId}' no encontrada`);
  if (statusMap.get(sessionId) !== 'open')
    throw new Error(`Sesión '${sessionId}' no está activa (estado: ${statusMap.get(sessionId)})`);
  return sock;
}

/**
 * Normaliza un número de teléfono al formato JID de WhatsApp.
 * Ejemplo: "3001234567" → "573001234567@s.whatsapp.net"
 */
function formatPhone(phone) {
  const clean = phone.replace(/\D/g, '');
  const withCode = clean.startsWith('57') ? clean : `57${clean}`;
  return `${withCode}@s.whatsapp.net`;
}

// ── Crear / restaurar sesión ──────────────────────────────────────────────────

/**
 * Inicia o restaura una sesión de WhatsApp con Baileys.
 * Si ya existe una sesión guardada en disco, se restaura automáticamente.
 * Si no, genera un QR para escanear.
 *
 * @param {string} sessionId - identificador único, ej: "admin" o "mecanico-5"
 */
export async function createSession(sessionId) {

  // Si ya está en memoria, retornar estado actual
  if (sessions.has(sessionId)) {
    return { status: statusMap.get(sessionId) };
  }

  const sessionPath = path.join(SESSION_DIR, sessionId);
  fs.mkdirSync(sessionPath, { recursive: true });

  const { state, saveCreds } = await useMultiFileAuthState(sessionPath);
  const { version }          = await fetchLatestBaileysVersion();

  const sock = makeWASocket({
    version,
    auth:              state,
    logger:            pino({ level: 'silent' }),
    printQRInTerminal: false,
    browser:           ['TurboMechanics', 'Chrome', '1.0.0'],
  });

  sessions.set(sessionId, sock);
  statusMap.set(sessionId, 'connecting');

  // ── Evento: cambios de conexión ───────────────────────────────────────────
  sock.ev.on('connection.update', async (update) => {
    const { connection, lastDisconnect, qr } = update;

    // QR disponible para escanear
    if (qr) {
      const qrBase64 = await qrcode.toDataURL(qr);
      qrCodes.set(sessionId, qrBase64);
      statusMap.set(sessionId, 'qr_pending');
      console.log(`[${sessionId}] QR generado, esperando escaneo...`);
    }

    // Conexión exitosa
    if (connection === 'open') {
      statusMap.set(sessionId, 'open');
      qrCodes.delete(sessionId);
      console.log(`[${sessionId}] ✅ WhatsApp conectado`);
    }

    // Desconexión
    if (connection === 'close') {
      const code           = new Boom(lastDisconnect?.error)?.output?.statusCode;
      const shouldReconnect = code !== DisconnectReason.loggedOut;

      console.log(`[${sessionId}] ❌ Desconectado. Código: ${code}`);
      sessions.delete(sessionId);
      statusMap.set(sessionId, 'closed');

      if (shouldReconnect) {
        // Reconectar automáticamente (ej: pérdida de red)
        console.log(`[${sessionId}] 🔄 Reconectando en 3 segundos...`);
        setTimeout(() => createSession(sessionId), 3000);
      } else {
        // Logout manual: borrar archivos de sesión
        console.log(`[${sessionId}] 🗑️ Sesión cerrada definitivamente, limpiando archivos...`);
        fs.rmSync(sessionPath, { recursive: true, force: true });
      }
    }
  });

  // ── Evento: guardar credenciales ──────────────────────────────────────────
  sock.ev.on('creds.update', saveCreds);

  // ── Evento: mensajes entrantes ────────────────────────────────────────────
  sock.ev.on('messages.upsert', async ({ messages: msgs, type }) => {
    if (type !== 'notify') return;

    for (const msg of msgs) {
      // Ignorar mensajes propios o vacíos
      if (!msg.message || msg.key.fromMe) continue;

      const from    = msg.key.remoteJid;
      const msgType = Object.keys(msg.message)[0];
      const body    = msg.message?.conversation
                   || msg.message?.extendedTextMessage?.text
                   || '';

      console.log(`[${sessionId}] 📨 Mensaje de ${from}: "${body}"`);

      // Notificar al backend Spring Boot
      try {
        await axios.post(
          `${SPRING_URL}/whatsapp/incoming`,
          { sessionId, from, messageType: msgType, body, timestamp: msg.messageTimestamp },
          { timeout: 5000 }
        );
      } catch (e) {
        console.warn(`[${sessionId}] No se pudo notificar a Spring Boot: ${e.message}`);
      }
    }
  });

  return { status: 'connecting' };
}

// ── Consultas de estado ───────────────────────────────────────────────────────

/**
 * Retorna el estado y el QR (si está disponible) de una sesión.
 */
export function getQR(sessionId) {
  return {
    status: statusMap.get(sessionId) || 'not_found',
    qr:     qrCodes.get(sessionId)   || null,
  };
}

/**
 * Retorna solo el estado de una sesión.
 */
export function getStatus(sessionId) {
  return statusMap.get(sessionId) || 'not_found';
}

// ── Cerrar sesión ─────────────────────────────────────────────────────────────

/**
 * Cierra y elimina una sesión de WhatsApp.
 * Borra los archivos de sesión del disco.
 */
export async function logoutSession(sessionId) {
  const sock = sessions.get(sessionId);
  if (sock) {
    try { await sock.logout(); } catch (_) {}
    sessions.delete(sessionId);
  }
  statusMap.set(sessionId, 'closed');
  qrCodes.delete(sessionId);
  return { success: true };
}

// ── Envío de mensajes ─────────────────────────────────────────────────────────

/**
 * Envía un mensaje de texto plano.
 *
 * @param {string} sessionId
 * @param {string} to        - número del destinatario, ej: "3001234567"
 * @param {string} message   - texto a enviar (soporta formato WhatsApp: *negrita*, _cursiva_)
 */
export async function sendText(sessionId, to, message) {
  const sock = getSocket(sessionId);
  const jid  = formatPhone(to);
  await sock.sendMessage(jid, { text: message });
  console.log(`[${sessionId}] 📤 Texto enviado a ${jid}`);
  return { success: true, to: jid };
}

/**
 * Envía un archivo PDF.
 *
 * @param {string} sessionId
 * @param {string} to
 * @param {string} filePath  - ruta temporal del archivo
 * @param {string} filename  - nombre que verá el receptor
 * @param {string} caption   - mensaje que acompaña el archivo
 */
export async function sendPDF(sessionId, to, filePath, filename, caption = '') {
  const sock   = getSocket(sessionId);
  const jid    = formatPhone(to);
  const buffer = fs.readFileSync(filePath);

  await sock.sendMessage(jid, {
    document: buffer,
    mimetype:  'application/pdf',
    fileName:  filename || path.basename(filePath),
    caption,
  });
  console.log(`[${sessionId}] 📄 PDF '${filename}' enviado a ${jid}`);
  return { success: true, to: jid };
}

/**
 * Envía un archivo Excel (.xlsx).
 *
 * @param {string} sessionId
 * @param {string} to
 * @param {string} filePath
 * @param {string} filename
 * @param {string} caption
 */
export async function sendExcel(sessionId, to, filePath, filename, caption = '') {
  const sock   = getSocket(sessionId);
  const jid    = formatPhone(to);
  const buffer = fs.readFileSync(filePath);

  await sock.sendMessage(jid, {
    document: buffer,
    mimetype:  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileName:  filename || path.basename(filePath),
    caption,
  });
  console.log(`[${sessionId}] 📊 Excel '${filename}' enviado a ${jid}`);
  return { success: true, to: jid };
}

/**
 * Envía una imagen.
 *
 * @param {string} sessionId
 * @param {string} to
 * @param {string} filePath
 * @param {string} caption
 */
export async function sendImage(sessionId, to, filePath, caption = '') {
  const sock   = getSocket(sessionId);
  const jid    = formatPhone(to);
  const buffer = fs.readFileSync(filePath);

  await sock.sendMessage(jid, { image: buffer, caption });
  console.log(`[${sessionId}] 🖼️ Imagen enviada a ${jid}`);
  return { success: true, to: jid };
}