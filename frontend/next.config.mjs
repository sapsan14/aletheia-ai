/** @type {import('next').NextConfig} */
const nextConfig = {
  // /api/* is proxied at runtime by app/api/[...path]/route.ts (uses BACKEND_INTERNAL_URL in container)
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
