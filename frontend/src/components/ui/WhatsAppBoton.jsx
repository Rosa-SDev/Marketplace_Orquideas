import { useState, useEffect } from 'react';
import api from '../../services/api';
import './WhatsAppBoton.css';

const WhatsAppBoton = () => {
    const [urlWhatsapp, setUrlWhatsapp] = useState('https://wa.me/573014791094');

    useEffect(() => {
        api.get('/contacto')
            .then(res => setUrlWhatsapp(res.data.urlWhatsapp))
            .catch(() => {});
    }, []);

    const handleClick = () => {
        window.open(urlWhatsapp, '_blank', 'noreferrer');
    };

    return (
        <div
            onClick={handleClick}
            className="whatsapp-boton"
            style={{ cursor: 'pointer' }}
        >
            <img
                src="/whatsapp.png"
                alt="Contacto por WhatsApp"
                className="whatsapp-icono"
            />
        </div>
    );
};

export default WhatsAppBoton;