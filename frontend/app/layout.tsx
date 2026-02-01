/**
 * Task 1.3 — Root layout (Next.js)
 *
 * This file wraps every page in the app. It defines:
 * - HTML structure (<html>, <body>)
 * - Fonts (Geist Sans & Mono)
 * - Global CSS (Tailwind, etc.)
 *
 * For beginners:
 * - "layout" means this component wraps all pages — every route inherits it
 * - {children} is where the current page content gets rendered (e.g. page.tsx)
 * - Metadata sets the browser tab title and SEO description
 */
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Aletheia AI — Verifiable AI Responses",
  description: "Cryptographically signed and timestamped LLM answers",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {children}
      </body>
    </html>
  );
}
