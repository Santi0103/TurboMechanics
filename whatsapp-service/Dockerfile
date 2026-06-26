FROM node:20-alpine
WORKDIR /app

COPY package*.json ./
RUN npm install --omit=dev

COPY src src

# Carpetas persistentes: sessions debe sobrevivir restarts (Volume en Railway)
RUN mkdir -p /app/sessions /app/temp

EXPOSE 3001

CMD ["node", "src/index.js"]
