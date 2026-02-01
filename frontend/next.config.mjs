/** @type {import('next').NextConfig} */
const nextConfig = {
  // /api/* is proxied at runtime by app/api/[...path]/route.ts (reads BACKEND_INTERNAL_URL
  // from process.env per request). No rewrites here so the same image works in Docker and locally.
  async headers() {
    return [
      {
        source: "/logo.png",
        headers: [
          {
            key: "Cache-Control",
            value: "no-cache, max-age=0, must-revalidate",
          },
        ],
      },
    ];
  },
};

export default nextConfig;
