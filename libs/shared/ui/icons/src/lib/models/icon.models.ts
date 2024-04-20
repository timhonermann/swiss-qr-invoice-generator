export const icons = ['document', 'xmark'] as const;

export type IconType = (typeof icons)[number];
