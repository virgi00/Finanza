# Dockerfile per Frontend React
FROM alpine:3.18 AS build

# Installa Node.js e npm
RUN apk add --no-cache nodejs npm

# Imposta la directory di lavoro
WORKDIR /app

# Copia i file di configurazione Node.js
COPY package*.json ./

# Installa le dipendenze
RUN npm ci --only=production

# Copia tutto il codice sorgente
COPY . .

# Imposta NODE_ENV su production per il rilevamento automatico dell'ambiente
ENV NODE_ENV=production

# Compila l'applicazione React per la produzione
RUN npm run build

# Stage di produzione con Nginx
FROM alpine:3.18

# Installa Nginx e crea la directory per i file web
RUN apk add --no-cache nginx && \
    mkdir -p /usr/share/nginx/html

# Copia la build di React nella directory di Nginx
COPY --from=build /app/build /usr/share/nginx/html

# Copia la configurazione personalizzata di Nginx (opzionale)
COPY nginx.conf /etc/nginx/nginx.conf

# Esponi la porta 80
EXPOSE 80

# Avvia Nginx
CMD ["nginx", "-g", "daemon off;"]
