# Image Node.js khfifa
FROM node:20-alpine

WORKDIR /app

# Copiyew les fichiers dyal les packages
COPY package.json package-lock.json* ./

# Installation dyal les dépendances (legacy-peer-deps bach maytbloquach m3a framer-motion awla lucide)
RUN npm install --legacy-peer-deps

# Copiyew l'code kaml
COPY . .

# Build l'application l'Production
RUN npm run build

# L'Port dyal Next.js
EXPOSE 3000

# Demarrage dyal Next.js
CMD ["npm", "run", "start"]