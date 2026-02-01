/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    const backend = process.env.BACKEND_INTERNAL_URL || "http://localhost:8080";
    return [{ source: "/api/:path*", destination: `${backend}/api/:path*` }];
  },
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
