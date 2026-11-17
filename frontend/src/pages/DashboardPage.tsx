import { useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Package, Tags, CircleDollarSign, TriangleAlert, ArrowUpRight, Plus, ArrowRight } from 'lucide-react';
import { useResource } from '../hooks/useResource';
import { dashboardService } from '../services/inventory';
import { Loading, ErrorPanel, Empty, money, dateLabel } from '../components/Feedback';

// All dashboard cards use totals computed by PostgreSQL, never paginated estimates.
export default function DashboardPage() {
  const load = useCallback((signal: AbortSignal) => dashboardService.get(signal), []);
  const { data, loading, error, reload } = useResource(load);
  return <>
    <div className="page-heading"><div><span className="eyebrow">YOUR INVENTORY, AT A GLANCE</span><h1>Overview<span className="heading-dot">.</span></h1><p>Everything you need to keep your stock moving.</p></div><Link className="button primary" to="/products?create=true"><Plus size={17}/> Add product</Link></div>
    {loading ? <Loading/> : error ? <ErrorPanel message={error} retry={reload}/> : data && <>
      <div className="stats-grid">{[
        {title:'Total products',value:data.totalProducts,icon:Package,detail:'Unique products in your catalog',className:'mint'},
        {title:'Categories',value:data.totalCategories,icon:Tags,detail:'Organized for a clearer view',className:'blue'},
        {title:'Inventory value',value:money(data.totalInventoryValue),icon:CircleDollarSign,detail:'Unit price × quantity · USD',className:'purple'},
        {title:'Low-stock items',value:data.lowStockCount,icon:TriangleAlert,detail:`Products with ${data.lowStockThreshold} units or fewer`,className:'amber'},
      ].map(({title,value,icon:Icon,detail,className}) => <section className="stat-card" key={title}><div className="stat-top"><span>{title}</span><span className={`stat-icon ${className}`}><Icon size={19}/></span></div><strong>{value}</strong><small>{detail}</small></section>)}</div>
      <div className="dashboard-grid"><section className="panel"><div className="panel-heading"><div><h2>Recently added</h2><p>The newest additions to your catalog.</p></div><Link className="text-link" to="/products">All products <ArrowUpRight size={16}/></Link></div>
        {data.recentProducts.length ? <div className="table-wrap"><table><thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Added</th></tr></thead><tbody>{data.recentProducts.map((product) => <tr key={product.id}><td><div className="product-cell"><span className="item-icon"><Package size={18}/></span><div><strong>{product.name}</strong><small>{product.sku}</small></div></div></td><td><span className="badge">{product.category.name}</span></td><td>{money(product.price)}</td><td className="muted nowrap">{dateLabel(product.createdAt)}</td></tr>)}</tbody></table></div> : <Empty>Create a category, then add your first product.</Empty>}
      </section><section className="panel stock-panel"><div className="panel-heading"><div><span className="eyebrow">STOCK WATCH</span><h2>Needs your attention</h2><p>Keep your shelves ready.</p></div><TriangleAlert className="amber-text" size={21}/></div>
        {data.lowStockProducts.length ? <div className="stock-list">{data.lowStockProducts.map((product) => <div key={product.id}><span className="stock-dot"/><div><strong>{product.name}</strong><small>{product.sku}</small></div><span className="stock-quantity">{product.quantity} left</span></div>)}</div> : <div className="stock-clear"><Package size={30}/><h3>You're all stocked up</h3><p>No low-stock products to review.</p></div>}
        <Link className="stock-link" to="/products?lowStock=true">Review low stock <ArrowRight size={16}/></Link>
      </section></div>
      <section className="bottom-banner"><div><span className="eyebrow">A PLACE FOR EVERYTHING</span><h2>A little organization goes a long way.</h2><p>Group products into categories to make your inventory easier to navigate.</p></div><Link className="button secondary" to="/categories">Manage categories <ArrowUpRight size={16}/></Link></section>
    </>}
  </>;
}
