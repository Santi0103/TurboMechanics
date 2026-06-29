import 'dotenv/config';
import express    from 'express';
import fs         from 'fs';
import path       from 'path';
import router     from './routes/messages.js';
import { createSession } from './whatsapp.js';

const app  = express();
const PORT = process.env.PORT || 3001;

// ── Middlewares ───────────────────────────────────────────────────────────────
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ── Rutas ─────────────────────────────────────────────────────────────────────
app.use('/api', router);

// Health check — útil para verificar que el servicio está corriendo
app.get('/health', (_, res) => res.json({ ok: true, timestamp: new Date().toISOString() }));

// ── Restaurar sesiones previas al arrancar ────────────────────────────────────
const SESSION_DIR = process.env.SESSION_DIR || './sessions';
fs.mkdirSync(SESSION_DIR, { recursive: true });
fs.mkdirSync(process.env.TEMP_DIR || './temp', { recursive: true });

const savedSessions = fs.readdirSync(SESSION_DIR, { withFileTypes: true })
  .filter(d => d.isDirectory() && d.name !== 'lost+found')
  .map(d => d.name);

if (savedSessions.length > 0) {
  console.log(`🔄 Restaurando ${savedSessions.length} sesión(es) guardada(s)...`);
  for (const sid of savedSessions) {
    createSession(sid).catch(e =>
      console.error(`Error restaurando sesión '${sid}': ${e.message}`)
    );
  }
} else {
  console.log('ℹ️  No hay sesiones previas. Usa POST /api/sessions/:id/start para conectar.');
}

// ── Arrancar servidor ─────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`🚀 WhatsApp Service corriendo en http://localhost:${PORT}`);
  console.log(`📡 Spring Boot URL: ${process.env.SPRING_BOOT_URL || 'http://localhost:9090'}`);
});