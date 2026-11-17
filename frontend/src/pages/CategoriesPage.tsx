import { useCallback, useState } from 'react';
import { Plus, Tags, Pencil, Trash2 } from 'lucide-react';
import { useResource } from '../hooks/useResource';
import { categoryService } from '../services/inventory';
import type { Category, CategoryInput } from '../types';
import { Loading, ErrorPanel, Empty, dateLabel } from '../components/Feedback';
import { CategoryForm } from '../components/CategoryForm';
import { Modal } from '../components/Modal';
import { DeleteDialog } from '../components/DeleteDialog';
import { useNotify } from '../components/Notifications';

// Category deletion errors stay visible when products still reference the category.
export default function CategoriesPage() {
  const load = useCallback((signal: AbortSignal) => categoryService.list(signal), []);
  const { data, loading, error, reload } = useResource(load);
  const [editing, setEditing] = useState<Category>(); const [showForm, setShowForm] = useState(false);
  const [deleting, setDeleting] = useState<Category>(); const [busy, setBusy] = useState(false);
  const notify = useNotify();
  function closeForm() { setShowForm(false); setEditing(undefined); }
  async function save(input: CategoryInput) {
    if (editing) await categoryService.update(editing.id, input); else await categoryService.create(input);
    notify(editing ? 'Category updated.' : 'Category created.'); closeForm(); reload();
  }
  return <>
    <div className="page-heading"><div><span className="eyebrow">A MORE ORGANIZED INVENTORY</span><h1>Categories<span className="heading-dot">.</span></h1><p>Give every product a place to belong.</p></div><button className="button primary" onClick={() => { setEditing(undefined); setShowForm(true); }}><Plus size={17}/> Add category</button></div>
    <section className="panel"><div className="panel-heading"><div className="inline-title"><h2>All categories</h2><span className="count-badge">{data?.length ?? '—'}</span></div><span className="muted small-text">Organize your catalog</span></div>
      {loading ? <Loading/> : error ? <ErrorPanel message={error} retry={reload}/> : !data?.length ? <Empty>Add your first category to start organizing products.</Empty> : <div className="table-wrap"><table><thead><tr><th>Category</th><th>Description</th><th>Created</th><th className="right">Actions</th></tr></thead><tbody>{data.map((category) => <tr key={category.id}><td><div className="product-cell"><span className="item-icon blue"><Tags size={18}/></span><strong>{category.name}</strong></div></td><td className="category-description">{category.description || <span className="muted">No description</span>}</td><td className="muted nowrap">{dateLabel(category.createdAt)}</td><td><div className="row-actions"><button className="icon-button" aria-label={`Edit ${category.name}`} onClick={() => { setEditing(category); setShowForm(true); }}><Pencil size={16}/></button><button className="icon-button delete-button" aria-label={`Delete ${category.name}`} onClick={() => setDeleting(category)}><Trash2 size={16}/></button></div></td></tr>)}</tbody></table></div>}
    </section><p className="page-note">Categories that contain products cannot be deleted. Move or remove their products first.</p>
    {showForm && <Modal title={editing ? 'Edit category' : 'Add a category'} onClose={closeForm} busy={busy}><CategoryForm category={editing} onSave={save} onCancel={closeForm} onBusy={setBusy}/></Modal>}
    {deleting && <DeleteDialog name={deleting.name} onClose={() => setDeleting(undefined)} onDelete={async () => { await categoryService.remove(deleting.id); notify('Category deleted.'); reload(); }}/>}
  </>;
}
