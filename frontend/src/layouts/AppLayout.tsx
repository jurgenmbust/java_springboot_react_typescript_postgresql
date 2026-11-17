import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { Boxes, LayoutDashboard, Package, Tags, Moon, Sun, ArrowUpRight, Menu, X } from 'lucide-react';

// Navigation and theme wrap all pages. Only the display preference uses local storage.
export default function AppLayout() {
  const [open, setOpen] = useState(false);
  const [dark, setDark] = useState(() => localStorage.getItem('inventory-theme') === 'dark');
  function toggleTheme() { setDark((current) => { localStorage.setItem('inventory-theme', current ? 'light' : 'dark'); return !current; }); }
  return <div className={dark ? 'app dark' : 'app'}>
    <a className="skip-link" href="#main">Skip to content</a>
    <div className="mobile-header"><span className="brand"><Boxes/> STOCKROOM<span className="brand-dot">.</span></span><button className="icon-button" aria-label={open ? 'Close navigation' : 'Open navigation'} aria-expanded={open} onClick={() => setOpen(!open)}>{open ? <X/> : <Menu/>}</button></div>
    <aside className={`sidebar ${open ? 'is-open' : ''}`}>
      <NavLink className="brand desktop-brand" to="/"><Boxes size={27}/> STOCKROOM<span className="brand-dot">.</span></NavLink>
      <div className="workspace-label"><span className="workspace-icon">IM</span><div><strong>Inventory workspace</strong><small>Local development</small></div><span className="live-dot"/></div>
      <p className="nav-caption">WORKSPACE</p>
      <nav aria-label="Main navigation">{[{to:'/',label:'Overview',icon:LayoutDashboard},{to:'/products',label:'Products',icon:Package},{to:'/categories',label:'Categories',icon:Tags}].map(({to,label,icon:Icon}) => <NavLink key={to} end={to === '/'} to={to} onClick={() => setOpen(false)}><Icon size={19}/>{label}</NavLink>)}</nav>
      <div className="sidebar-bottom"><div className="learning-note"><span className="eyebrow">BUILT TO LEARN</span><p>A clear view of your inventory.<br/>A closer look at full-stack.</p><a href="https://docs.spring.io/spring-boot/" target="_blank" rel="noreferrer">Explore Spring Boot <ArrowUpRight size={14}/></a></div><button className="theme-button" onClick={toggleTheme}>{dark ? <Sun size={18}/> : <Moon size={18}/>} {dark ? 'Switch to light mode' : 'Switch to dark mode'}</button><div className="profile"><span>LP</span><div><strong>Learning project</strong><small>Inventory Management System</small></div></div></div>
    </aside>
    <div className="main-shell"><div className="topbar"><span>Workspace <span className="slash">/</span> Inventory Management System</span><span className="local-badge"><span className="live-dot"/> Local workspace</span></div><main id="main" tabIndex={-1}><Outlet/></main><footer>INVENTORY MANAGEMENT SYSTEM <span>Java · Spring Boot · React · PostgreSQL</span></footer></div>
  </div>;
}
