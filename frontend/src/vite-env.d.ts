/// <reference types="vite/client" />

declare module "*.css" {
  const content: Record<string, string>;
  export default content;
}

declare module "*.mjs" {
  const value: any;
  export default value;
  export const animate: any;
  export const stagger: any;
}

declare module "./vendor/motion.min.js" {
  export const animate: any;
  export const stagger: any;
}
