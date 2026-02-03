# Aletheia AI — Frontend

Next.js frontend for verifiable AI responses. Prompt input, Send button, Response area with status (Signed, Timestamped, Verifiable), link to the verify page, and **Download evidence** to export the Evidence Package (`.aep`) for offline verification. After asking a question, click **Download evidence** to get the Evidence Package for the last response; the file is saved as `aletheia-evidence-<id>.aep`.

## Testing via frontend (avoid 501 Unsupported method POST)

The **Send** button sends a POST request. You must use the **Next.js dev server** so that `/api/*` routes (and POST) are handled correctly. Do **not** open the built app with a static file server (e.g. `npx serve`, `python -m http.server`) or `file://` — those do not support POST and will return **501 Unsupported method**.

1. **Start the backend** (from repo root or `backend/`):
   ```bash
   cd backend && mvn spring-boot:run
   ```
   Backend runs at `http://localhost:8080`.

2. **Start the frontend** (from `frontend/`):
   ```bash
   npm run dev
   ```
   Open [http://localhost:3000](http://localhost:3000).

3. **Optional:** Copy `.env.example` to `.env` and set `NEXT_PUBLIC_API_URL=http://localhost:8080` if you want the browser to call the backend directly (backend CORS allows `http://localhost:3000`). If you leave it unset, the browser calls the same origin and Next.js proxies requests to the backend.

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
