export const icons = ['document'] as const;

export type IconType = (typeof icons)[number];
