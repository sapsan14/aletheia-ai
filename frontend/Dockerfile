# =============================================================================
# Aletheia AI Frontend — Multi-stage Dockerfile (Next.js)
# =============================================================================
# Build with: docker build --build-arg NEXT_PUBLIC_API_URL=http://localhost:8080
# Run: next start (port 3000)
# NEXT_PUBLIC_API_URL must be set at build time (baked into client bundle)
# =============================================================================

# --- Stage 1: Build ---
FROM node:20-alpine AS builder

WORKDIR /app

# Build-time API URL (required for client-side fetch)
ARG NEXT_PUBLIC_API_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

# Install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# --- Stage 2: Runtime ---
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Copy built app (next.config.ts required at runtime for headers())
COPY --from=builder /app/package.json ./
COPY --from=builder /app/package-lock.json ./
COPY --from=builder /app/next.config.ts ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

# Production deps only
RUN npm ci --omit=dev

EXPOSE 3000

CMD ["npm", "start"]
