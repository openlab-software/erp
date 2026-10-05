# Stage 1: deps
FROM node:22-alpine AS deps
WORKDIR /app

RUN corepack enable

COPY package.json yarn.lock .yarnrc.yml ./
RUN yarn install --immutable

# Stage 2: build
FROM node:22-alpine AS builder
WORKDIR /app

RUN corepack enable

COPY --from=deps /app/node_modules ./node_modules
COPY . .

RUN yarn build

# Stage 3: production
FROM node:22-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production

RUN corepack enable

COPY package.json yarn.lock .yarnrc.yml ./
RUN yarn workspaces focus --production

COPY --from=builder /app/.output ./.output

EXPOSE 8080

CMD ["node", ".output/index.js"]
