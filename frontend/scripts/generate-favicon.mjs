#!/usr/bin/env node
/**
 * Generate favicon.ico from project logo (public/logo1.png, transparent).
 * Output: app/favicon.ico (multi-size 16, 32, 48).
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import sharp from 'sharp';
import pngToIco from 'png-to-ico';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');
const logoPath = path.join(root, 'public', 'logo1.png');
const outPath = path.join(root, 'app', 'favicon.ico');

const SIZES = [16, 32, 48];
/** Trim: pixels within this distance from top-left (the padding color) are removed. */
const TRIM_THRESHOLD = 32;

async function main() {
  const buf = await fs.promises.readFile(logoPath);
  // Trim edges that match the corner (transparent or padding color)
  const trimmed = await sharp(buf)
    .trim({ threshold: TRIM_THRESHOLD })
    .toBuffer();

  const pngs = await Promise.all(
    SIZES.map((size) =>
      sharp(trimmed)
        .resize(size, size)
        .ensureAlpha()
        .png()
        .toBuffer()
    )
  );
  const ico = await pngToIco(pngs);
  await fs.promises.writeFile(outPath, ico);
  console.log('Wrote', outPath, `(${SIZES.join(', ')}px)`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
