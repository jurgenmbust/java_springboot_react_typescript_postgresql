import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Plus, Search, SlidersHorizontal, Pencil, Trash2, Package, ChevronLeft, ChevronRight } from 'lucide-react';
import { useResource } from '../hooks/useResource';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import { categoryService, productService } from '../services/inventory';
import type { Product, ProductInput } from '../types';
import { Loading, ErrorPanel, Empty, money } from '../components/Feedback';
import { ProductForm } from '../components/ProductForm';
import { Modal } from '../components/Modal';
import { DeleteDialog } from '../components/DeleteDialog';
import { useNotify } from '../components/Notifications';

// Owns filters and selected records. Services handle HTTP and forms own draft input.
export default function ProductsPage() {
  const [params, setParams] = useSearchParams();
  const [query, setQuery] = useState(''); const search = useDebouncedValue(query);
  const [categoryId, setCategoryId] = useState(''); const [lowStock, setLowStock] = useState(params.get('lowStock') === 'true');
  const [sort, setSort] = useState('createdAt'); const [direction, setDirection] = useState('desc');
  const [page, setPage] = useState(0); const [size, setSize] = useState(10);
  const [editing, setEditing] = useState<Product>(); const [showForm, setShowForm] = useState(params.get('create') === 'true');
  const [deleting, setDeleting] = useState<Product>(); const [busy, setBusy] = useState(false);
  const notify = useNotify();
  const load = useCallback((signal: AbortSignal) => productService.list({ search, categoryId, lowStock, page, size, sort, direction }, signal), [search, categoryId, lowStock, page, size, sort, direction]);
  const list = useResource(load);
  const loadCategories = useCallback((signal: AbortSignal) => categoryService.list(signal), []);
  const categories = useResource(loadCategories);
  // If the last row on a page is removed, return to the previous available page.
  useEffect(() => { if (!list.loading && list.data && page > 0 && page >= list.data.totalPages) setPage(Math.max(0, list.data.totalPages - 1)); }, [list.loading, list.data, page]);
  function closeForm() { setShowForm(false); setEditing(undefined); if (params.has('create')) { params.delete('create'); setParams(params, { replace: true }); } }
  async function save(input: ProductInput) {
    if (editing) await productService.update(editing.id, input); else await productService.create(input);
    notify(editing ? 'Product updated.' : 'Product created.'); closeForm(); list.reload();
  }
  return <>
    <div className="page-heading"><div><span className="eyebrow">THE PRODUCT CATALOG</span><h1>Products<span className="heading-dot">.</span></h1><p>Keep every item, price and quantity in one place.</p></div><button className="button primary" onClick={() => { setEditing(undefined); setShowForm(true); }}><Plus size={17}/> Add product</button></div>
    {categories.error && <ErrorPanel message={categories.error} retry={categories.reload}/>}
    <section className="panel"><div className="panel-heading"><div className="inline-title"><h2>All products</h2><span className="count-badge">{list.data?.totalElements ?? '—'}</span></div><span className="muted small-text">Your live inventory</span></div>
      <div className="filters"><div className="search-input"><Search size={17}/><input aria-label="Search products by name" placeholder="Search products by name…" value={query} maxLength={120} onChange={(e) => { setQuery(e.target.value); setPage(0); }}/></div>
        <select aria-label="Filter by category" value={categoryId} onChange={(e) => { setCategoryId(e.target.value); setPage(0); }}><option value="">All categories</option>{categories.data?.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select>
        <label className="check-filter"><input type="checkbox" checked={lowStock} onChange={(e) => { setLowStock(e.target.checked); setPage(0); }}/><SlidersHorizontal size={15}/> Low stock</label>
        <select aria-label="Sort products" value={`${sort}:${direction}`} onChange={(e) => { const [field, order] = e.target.value.split(':'); setSort(field); setDirection(order); setPage(0); }}>
          <option value="createdAt:desc">Newest first</option><option value="createdAt:asc">Oldest first</option><option value="name:asc">Name A–Z</option><option value="name:desc">Name Z–A</option><option value="price:asc">Price low–high</option><option value="price:desc">Price high–low</option>
        </select>
      </div>
      {list.loading ? <Loading/> : list.error ? <ErrorPanel message={list.error} retry={list.reload}/> : !list.data?.content.length ? <Empty>{query || categoryId || lowStock ? 'No products match these filters. Try adjusting your search.' : 'Create a category, then add your first product.'}</Empty> : <div className="table-wrap"><table><thead><tr><th>Product name</th><th>SKU</th><th>Category</th><th>Price</th><th>Quantity</th><th className="right">Actions</th></tr></thead><tbody>{list.data.content.map((product) => <tr key={product.id}>
        <td><div className="product-cell"><span className="item-icon"><Package size={18}/></span><div><strong>{product.name}</strong><small className="description-preview">{product.description || 'No description'}</small></div></div></td><td><code>{product.sku}</code></td><td><span className="badge">{product.category.name}</span></td><td className="nowrap">{money(product.price)}</td><td><span className={`quantity ${product.quantity === 0 ? 'out' : ''}`}><span/>{product.quantity} units</span></td><td><div className="row-actions"><button className="icon-button" aria-label={`Edit ${product.name}`} onClick={() => { setEditing(product); setShowForm(true); }}><Pencil size={16}/></button><button className="icon-button delete-button" aria-label={`Delete ${product.name}`} onClick={() => setDeleting(product)}><Trash2 size={16}/></button></div></td>
      </tr>)}</tbody></table></div>}
      <div className="pagination"><span>{list.data?.totalElements ?? 0} products <span className="muted">· Page {page + 1} of {Math.max(1, list.data?.totalPages ?? 1)}</span></span><div><label htmlFor="page-size">Rows</label><select id="page-size" value={size} onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}>{[10,25,50].map((count) => <option key={count}>{count}</option>)}</select><button className="icon-button" aria-label="Previous page" disabled={page === 0 || list.loading} onClick={() => setPage(page - 1)}><ChevronLeft size={18}/></button><button className="icon-button" aria-label="Next page" disabled={list.loading || page + 1 >= (list.data?.totalPages ?? 0)} onClick={() => setPage(page + 1)}><ChevronRight size={18}/></button></div></div>
    </section>
    {showForm && <Modal title={editing ? 'Edit product' : 'Add a product'} onClose={closeForm} busy={busy}>{categories.loading ? <Loading/> : categories.error ? <ErrorPanel message={categories.error} retry={categories.reload}/> : !categories.data?.length ? <div className="empty"><p>Every product needs a category.</p><Link className="button primary" to="/categories">Create a category first</Link></div> : <ProductForm product={editing} categories={categories.data} onSave={save} onCancel={closeForm} onBusy={setBusy}/>}</Modal>}
    {deleting && <DeleteDialog name={deleting.name} onClose={() => setDeleting(undefined)} onDelete={async () => { await productService.remove(deleting.id); notify('Product deleted.'); list.reload(); }}/>}
  </>;
}
