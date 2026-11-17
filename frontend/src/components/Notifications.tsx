import { createContext, useContext, useEffect, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import { CheckCircle2, X } from 'lucide-react';
const Context = createContext<(message: string) => void>(() => {});

// Shares short success notifications between pages.
export function Notifications({ children }: { children: ReactNode }) {
  const [message, setMessage] = useState('');
  const timer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  useEffect(() => () => clearTimeout(timer.current), []);
  function notify(text: string) {
    clearTimeout(timer.current); setMessage(text);
    timer.current = setTimeout(() => setMessage(''), 5000);
  }
  return <Context.Provider value={notify}>{children}{message && <div className="toast" role="status"><CheckCircle2 size={18}/>{message}<button className="icon-button" aria-label="Dismiss notification" onClick={() => setMessage('')}><X size={16}/></button></div>}</Context.Provider>;
}
export const useNotify = () => useContext(Context);
