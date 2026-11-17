import { useEffect, useState } from 'react';
import { errorMessage } from '../services/api';

// This hook loads server data and cancels stale requests when filters change.
// load must be a stable function, usually created with useCallback.
export function useResource<T>(load: (signal: AbortSignal) => Promise<T>) {
  const [data, setData] = useState<T>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError('');
    load(controller.signal)
      .then((result) => { if (!controller.signal.aborted) setData(result); })
      .catch((reason: unknown) => { if (!controller.signal.aborted) setError(errorMessage(reason)); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [load, revision]);
  return { data, loading, error, reload: () => setRevision((value) => value + 1) };
}
