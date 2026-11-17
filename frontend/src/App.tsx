import { BrowserRouter, Link, Route, Routes } from 'react-router-dom';
import AppLayout from './layouts/AppLayout';
import DashboardPage from './pages/DashboardPage';
import ProductsPage from './pages/ProductsPage';
import CategoriesPage from './pages/CategoriesPage';
import { Notifications } from './components/Notifications';

// React Router replaces the page content without reloading the whole application.
export default function App() {
  return <BrowserRouter><Notifications><Routes><Route element={<AppLayout/>}>
    <Route index element={<DashboardPage/>}/><Route path="products" element={<ProductsPage/>}/><Route path="categories" element={<CategoriesPage/>}/>
    <Route path="*" element={<div className="empty"><h1>Page not found</h1><Link className="button primary" to="/">Back to overview</Link></div>}/>
  </Route></Routes></Notifications></BrowserRouter>;
}
