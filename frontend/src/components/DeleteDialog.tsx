import { useState } from 'react';
import { Modal } from './Modal';
import { ErrorPanel } from './Feedback';
import { errorMessage } from '../services/api';

// Keeps the confirmation open when the server rejects a deletion.
export function DeleteDialog({ name, onDelete, onClose }: { name: string; onDelete: () => Promise<void>; onClose: () => void }) {
  const [busy, setBusy] = useState(false); const [error, setError] = useState('');
  async function remove() {
    setBusy(true); setError('');
    try { await onDelete(); onClose(); }
    catch (reason) { setError(errorMessage(reason)); }
    finally { setBusy(false); }
  }
  return <Modal title="Delete record?" onClose={onClose} busy={busy}>
    <p className="muted">You are about to delete <strong>{name}</strong>. This action cannot be undone.</p>
    {error && <ErrorPanel message={error}/>}
    <div className="form-actions"><button className="button secondary" disabled={busy} onClick={onClose}>Cancel</button><button className="button danger" disabled={busy} onClick={remove}>{busy ? 'Deleting…' : 'Delete record'}</button></div>
  </Modal>;
}
