import { useEffect, useState } from 'react';
// Waits briefly before starting a search so each keystroke does not send a request.
export function useDebouncedValue<T>(value: T, delay = 300) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => { const timeout = setTimeout(() => setDebounced(value), delay); return () => clearTimeout(timeout); }, [value, delay]);
  return debounced;
}
