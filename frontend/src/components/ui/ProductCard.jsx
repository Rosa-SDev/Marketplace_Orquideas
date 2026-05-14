import './ProductCard.css';
import Button from './Button';
import { Link } from 'react-router-dom';
import useLazyAddToCart from '../../hooks/useLazyAddToCart';

const ProductCard = ({ id, nombre, precio, imagen, badge, stock, stockReservado = 0, tipo = 'orquidea' }) => {
    const stockDisponible = stock - stockReservado;

  // Agregar al carrito con login lazy
  const { agregarConLoginLazy } = useLazyAddToCart();

  const cardContent = (
    <div className={`product-card ${stockDisponible === 0 ? 'agotado' : ''}`}>

      <div className={`product-card-imagen-wrapper ${stockDisponible === 0 ? 'agotado' : ''}`}>
        <img
          src={imagen || 'https://placehold.co/240x200?text=Orquidea'}
          alt={nombre}
          className="product-card-imagen"
        />

        {badge && (
          <span className="product-card-badge">{badge}</span>
        )}
      </div>

      <div className="product-card-info">

        <h3 className="product-card-nombre">{nombre}</h3>

        <p className="product-card-precio">
          ${precio?.toLocaleString('es-CO')}
        </p>

        <p className={`product-card-stock ${stockDisponible > 0 ? 'disponible' : 'agotado'}`}>
          {stockDisponible > 0 ? `${stockDisponible} disponibles` : 'Agotado'}
        </p>

        <Button
          text="Agregar al carrito"
          disabled={stockDisponible === 0}
          onClick={(event) => {
            const hasSession = Boolean(localStorage.getItem('token'));
            const isDetailLink = Boolean(id && tipo === 'orquidea');

            if (!hasSession && isDetailLink) {
              return;
            }

            if (isDetailLink) {
              event.preventDefault();
              event.stopPropagation();
            }

            agregarConLoginLazy({ id, nombre, precio, imagen, stock });
          }}
        />

      </div>
    </div>
  );

  return (
    id && tipo === 'orquidea' ? (
      <Link to={`/orquideas/${id}`} style={{ textDecoration: 'none' }}>
        {cardContent}
      </Link>
    ) : (
      cardContent
    )
  );
};

export default ProductCard;