import express from "express";
import { createServer } from "node:http";
import { Server } from "socket.io";
import { WebcastPushConnection } from "tiktok-live-connector";

const app = express();
const httpServer = createServer(app);
const io = new Server(httpServer, {
  cors: { origin: "*", methods: ["GET", "POST"] },
  transports: ["websocket", "polling"],
});
const PORT = Number(process.env.PORT || 3000);
const liveConnections = new Map();

app.get("/", (_req, res) => res.type("text").send("Lula x Bolsonaro LIVE server is running."));
app.get("/api/health", (_req, res) => {
  res.json({
    status: "ok",
    activeLives: [...liveConnections.keys()],
    timestamp: new Date().toISOString(),
  });
});

function cleanUsername(value) {
  return String(value || "").trim().replace(/^@/, "").toLowerCase().replace(/[^a-z0-9._]/g, "");
}

function normalizeUser(raw) {
  return {
    uniqueId: raw.uniqueId || "",
    nickname: raw.nickname || raw.uniqueId || "Espectador",
    profilePictureUrl: raw.profilePictureUrl || "",
  };
}

async function connectLive(username) {
  const existing = liveConnections.get(username);
  if (existing) {
    if (existing.connecting) await existing.connecting;
    return existing;
  }

  const connection = new WebcastPushConnection(username, {
    processInitialData: true,
    enableExtendedGiftInfo: true,
    enableWebsocketUpgrade: true,
    requestPollingIntervalMs: 2000,
  });
  const record = { connection, connecting: null };
  liveConnections.set(username, record);

  connection.on("gift", (raw) => {
    io.to(username).emit("tiktok_gift", {
      user: normalizeUser(raw),
      giftId: Number(raw.giftId || 0),
      giftName: raw.giftName || raw.giftDetails?.giftName || "Presente",
      giftValue: Number(raw.diamondCount || raw.giftValue || 1),
      repeatCount: Number(raw.repeatCount || 1),
      giftType: Number(raw.diamondCount || raw.giftValue || 1) >= 100 ? "large" :
        Number(raw.diamondCount || raw.giftValue || 1) >= 10 ? "medium" : "small",
      timestamp: Date.now(),
    });
  });
  connection.on("chat", (raw) => {
    io.to(username).emit("tiktok_chat", {
      user: normalizeUser(raw),
      comment: String(raw.comment || ""),
      timestamp: Date.now(),
    });
  });
  connection.on("like", (raw) => {
    io.to(username).emit("tiktok_like", {
      user: normalizeUser(raw),
      likeCount: Number(raw.likeCount || 0),
      totalLikeCount: Number(raw.totalLikeCount || 0),
      timestamp: Date.now(),
    });
  });
  connection.on("connected", (state) => {
    io.to(username).emit("tiktok_connected", { roomId: state?.roomId || null, timestamp: Date.now() });
  });
  connection.on("disconnected", () => {
    io.to(username).emit("tiktok_disconnected", { timestamp: Date.now() });
    liveConnections.delete(username);
  });
  connection.on("error", (error) => {
    io.to(username).emit("tiktok_error", { message: String(error?.message || error), timestamp: Date.now() });
  });

  record.connecting = connection.connect();
  try {
    await record.connecting;
    record.connecting = null;
    return record;
  } catch (error) {
    liveConnections.delete(username);
    throw error;
  }
}

io.on("connection", (socket) => {
  socket.on("join-room", async (rawUsername) => {
    const username = cleanUsername(rawUsername);
    if (!username || username.length < 2) {
      socket.emit("connection-error", { message: "Digite um usuário válido do TikTok, sem @." });
      return;
    }
    socket.tiktokUsername = username;
    socket.join(username);
    try {
      await connectLive(username);
      socket.emit("room-joined", { room: username, message: "Conectado ao canal de eventos." });
    } catch (error) {
      socket.emit("connection-error", {
        message: "Não foi possível conectar a @" + username + ". Confirme que a conta está AO VIVO e tente novamente.",
      });
      console.error("TikTok LIVE connection failed for", username, error?.message || error);
    }
  });

  socket.on("leave-room", (rawUsername) => {
    const username = cleanUsername(rawUsername || socket.tiktokUsername);
    if (username) socket.leave(username);
  });
});

httpServer.listen(PORT, "0.0.0.0", () => {
  console.log("Lula x Bolsonaro LIVE server listening on port " + PORT);
});
