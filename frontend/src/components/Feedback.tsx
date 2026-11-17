import { AlertCircle, LoaderCircle, PackageOpen } from 'lucide-react';
import type { ReactNode } from 'react';
export function Loading() { return <div className="empty" role="status"><LoaderCircle className="animate-spin" size={24}/><p>Loading inventory…</p></div>; }
export function ErrorPanel({ message, retry }: { message: string; retry?: () => void }) { return <div className="error-panel" role="alert"><AlertCircle size={18}/><span>{message}</span>{retry && <button className="button secondary" onClick={retry}>Try again</button>}</div>; }
export function Empty({ children }: { children: ReactNode }) { return <div className="empty"><PackageOpen size={32}/><h3>Nothing here yet</h3><p>{children}</p></div>; }
// Money is stored and transported as a decimal string; conversion is only for display.
export const money = (value: string) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value));
export const dateLabel = (value: string) => new Date(value + 'Z').toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
