import './ProductCard.css';
import Button from './Button';
import { Link } from 'react-router-dom';
import useLazyAddToCart from '../../hooks/useLazyAddToCart';

const ProductCard = ({ id, nombre, precio, imagen, badge, stock, tipo = 'orquidea' }) => {

  // Agregar al carrito con login lazy
  const { agregarConLoginLazy } = useLazyAddToCart();

  const cardContent = (
    <div className="product-card">

      <div className="product-card-imagen-wrapper">
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

        <p className={`product-card-stock ${stock > 0 ? 'disponible' : 'agotado'}`}>
          {stock > 0 ? `${stock} disponibles` : 'Agotado'}
        </p>

        <Button
          text="Agregar al carrito"
          onClick={(event) => {
            event.stopPropagation();
            agregarConLoginLazy({ id, nombre, precio, imagen, stock });
          }}
        />

      </div>
    </div>
  );

  return (
    id && tipo === 'orquidea' ? (
      <Link to={`/orquidea/${id}`} style={{ textDecoration: 'none' }}>
        {cardContent}
      </Link>
    ) : (
      cardContent
    )
  );
};

export default ProductCard;