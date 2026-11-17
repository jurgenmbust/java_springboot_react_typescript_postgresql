import { useState } from 'react';
import type { FormEvent } from 'react';
import type { Category, CategoryInput } from '../types';
import { ApiError, errorMessage } from '../services/api';
import { ErrorPanel } from './Feedback';

// Category fields stay local until the API confirms the save.
export function CategoryForm({ category, onSave, onCancel, onBusy }: { category?: Category; onSave: (input: CategoryInput) => Promise<void>; onCancel: () => void; onBusy: (busy: boolean) => void }) {
  const [name, setName] = useState(category?.name || '');
  const [description, setDescription] = useState(category?.description || '');
  const [fieldError, setFieldError] = useState(''); const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent) {
    event.preventDefault(); setFieldError(''); setError('');
    if (!name.trim() || name.trim().length > 80) { setFieldError('Enter a name with 1–80 characters.'); return; }
    setBusy(true); onBusy(true);
    try { await onSave({ name: name.trim(), description: description.trim() }); }
    catch (reason) { setError(errorMessage(reason)); if (reason instanceof ApiError) setFieldError(reason.fieldErrors.name || ''); }
    finally { setBusy(false); onBusy(false); }
  }
  return <form noValidate onSubmit={submit}>{error && <ErrorPanel message={error}/>}<fieldset disabled={busy} className="form-grid">
    <div className="full"><label htmlFor="category-name">Category name *</label><input autoFocus id="category-name" value={name} onChange={(e) => setName(e.target.value)} maxLength={80} aria-invalid={!!fieldError} aria-describedby="category-name-error" placeholder="e.g. Office supplies"/><small className="field-error" id="category-name-error">{fieldError}</small></div>
    <div className="full"><label htmlFor="category-description">Description</label><textarea id="category-description" value={description} onChange={(e) => setDescription(e.target.value)} rows={4} maxLength={1000}/></div>
    <div className="form-actions full"><button type="button" className="button secondary" onClick={onCancel}>Cancel</button><button className="button primary" type="submit">{busy ? 'Saving…' : category ? 'Save changes' : 'Create category'}</button></div>
  </fieldset></form>;
}
