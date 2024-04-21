export const icons = ['document', 'xmark', 'bin'] as const;

export type IconType = (typeof icons)[number];
