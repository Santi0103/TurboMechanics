import express      from 'express';
import multer       from 'multer';
import fs           from 'fs';
import path         from 'path';
import {
  createSession,
  getQR,
  getStatus,
  sendText,
  sendPDF,
  sendExcel,
  sendImage,
  logoutSession,
} from '../whatsapp.js';

const router = express.Router();
const TEMP   = process.env.TEMP_DIR || './temp';
fs.mkdirSync(TEMP, { recursive: true });

// Multer: guarda archivos subidos en /temp con nombre único
const upload = multer({
  dest:   TEMP,
  limits: { fileSize: 25 * 1024 * 1024 }, // límite 25 MB
});

// ── Middleware: validar token de servicio ─────────────────────────────────────
router.use((req, res, next) => {
  const token = req.headers['x-service-token'];
  if (token !== process.env.SERVICE_TOKEN) {
    return res.status(401).json({ error: 'Token inválido' });
  }
  next();
});

// ── Sesiones ──────────────────────────────────────────────────────────────────

/**
 * POST /api/sessions/:sessionId/start
 * Inicia o restaura una sesión de WhatsApp.
 * Body: ninguno
 */
router.post('/sessions/:sessionId/start', async (req, res) => {
  try {
    const result = await createSession(req.params.sessionId);
    res.json(result);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

/**
 * GET /api/sessions/:sessionId/qr
 * Retorna el QR en base64 y el estado de la sesión.
 * Respuesta: { status, qr }
 */
router.get('/sessions/:sessionId/qr', (req, res) => {
  res.json(getQR(req.params.sessionId));
});

/**
 * GET /api/sessions/:sessionId/status
 * Retorna solo el estado de la sesión.
 * Respuesta: { status }
 */
router.get('/sessions/:sessionId/status', (req, res) => {
  res.json({ status: getStatus(req.params.sessionId) });
});

/**
 * DELETE /api/sessions/:sessionId
 * Cierra y elimina una sesión.
 */
router.delete('/sessions/:sessionId', async (req, res) => {
  try {
    await logoutSession(req.params.sessionId);
    res.json({ success: true });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// ── Mensajes ──────────────────────────────────────────────────────────────────

/**
 * POST /api/messages/text
 * Envía un mensaje de texto.
 * Body JSON: { sessionId, to, message }
 */
router.post('/messages/text', async (req, res) => {
  try {
    const { sessionId, to, message } = req.body;
    if (!sessionId || !to || !message)
      return res.status(400).json({ error: 'sessionId, to y message son obligatorios' });

    const result = await sendText(sessionId, to, message);
    res.json(result);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

/**
 * POST /api/messages/pdf
 * Envía un PDF adjunto.
 * Form-data: sessionId, to, caption (opcional), file (PDF)
 */
router.post('/messages/pdf', upload.single('file'), async (req, res) => {
  const tempPath = req.file?.path;
  try {
    const { sessionId, to, caption } = req.body;
    if (!sessionId || !to || !req.file)
      return res.status(400).json({ error: 'sessionId, to y file son obligatorios' });

    const filename = req.file.originalname || 'documento.pdf';
    const result   = await sendPDF(sessionId, to, tempPath, filename, caption);
    res.json(result);
  } catch (e) {
    res.status(500).json({ error: e.message });
  } finally {
    // Limpiar archivo temporal siempre
    if (tempPath && fs.existsSync(tempPath)) fs.unlinkSync(tempPath);
  }
});

/**
 * POST /api/messages/excel
 * Envía un archivo Excel adjunto.
 * Form-data: sessionId, to, caption (opcional), file (XLSX)
 */
router.post('/messages/excel', upload.single('file'), async (req, res) => {
  const tempPath = req.file?.path;
  try {
    const { sessionId, to, caption } = req.body;
    if (!sessionId || !to || !req.file)
      return res.status(400).json({ error: 'sessionId, to y file son obligatorios' });

    const filename = req.file.originalname || 'reporte.xlsx';
    const result   = await sendExcel(sessionId, to, tempPath, filename, caption);
    res.json(result);
  } catch (e) {
    res.status(500).json({ error: e.message });
  } finally {
    if (tempPath && fs.existsSync(tempPath)) fs.unlinkSync(tempPath);
  }
});

/**
 * POST /api/messages/image
 * Envía una imagen adjunta.
 * Form-data: sessionId, to, caption (opcional), file (imagen)
 */
router.post('/messages/image', upload.single('file'), async (req, res) => {
  const tempPath = req.file?.path;
  try {
    const { sessionId, to, caption } = req.body;
    if (!sessionId || !to || !req.file)
      return res.status(400).json({ error: 'sessionId, to y file son obligatorios' });

    const result = await sendImage(sessionId, to, tempPath, caption);
    res.json(result);
  } catch (e) {
    res.status(500).json({ error: e.message });
  } finally {
    if (tempPath && fs.existsSync(tempPath)) fs.unlinkSync(tempPath);
  }
});

export default router;