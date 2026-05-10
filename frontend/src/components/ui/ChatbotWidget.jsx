import { useState, useRef, useEffect } from 'react';
import api from '../../services/api';
import './ChatbotWidget.css';

const ChatbotWidget = () => {
    const [abierto, setAbierto] = useState(false);
    const [mensajes, setMensajes] = useState([
        {
            rol: 'bot',
            texto: '¡Hola! 🌸 Soy el asistente de Orquídeas del Combeima. ¿En qué puedo ayudarte hoy?',
        },
    ]);
    const [input, setInput] = useState('');
    const [cargando, setCargando] = useState(false);
    const bottomRef = useRef(null);

    useEffect(() => {
        if (abierto) {
            bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
        }
    }, [mensajes, abierto]);

    const enviarMensaje = async () => {
        const texto = input.trim();
        if (!texto || cargando) return;

        setMensajes((prev) => [...prev, { rol: 'usuario', texto }]);
        setInput('');
        setCargando(true);

        try {
            const res = await api.post('/chatbot', { mensaje: texto });
            setMensajes((prev) => [
                ...prev,
                { rol: 'bot', texto: res.data.respuesta },
            ]);
        } catch (error) {
            setMensajes((prev) => [
                ...prev,
                { rol: 'bot', texto: 'Lo siento, tuve un problema. ¿Puedes intentarlo de nuevo? 🌸' },
            ]);
        } finally {
            setCargando(false);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            enviarMensaje();
        }
    };

    return (
        <div className="chatbot-widget">
            {abierto && (
                <div className="chatbot-ventana">
                    <div className="chatbot-header">
                        <span>🌸 Asistente Orquídeas</span>
                        <button className="chatbot-cerrar" onClick={() => setAbierto(false)}>✕</button>
                    </div>

                    <div className="chatbot-mensajes">
                        {mensajes.map((msg, i) => (
                            <div key={i} className={`chatbot-mensaje chatbot-mensaje--${msg.rol}`}>
                                {msg.texto}
                            </div>
                        ))}
                        {cargando && (
                            <div className="chatbot-mensaje chatbot-mensaje--bot chatbot-cargando">
                                <span></span><span></span><span></span>
                            </div>
                        )}
                        <div ref={bottomRef} />
                    </div>

                    <div className="chatbot-input-area">
                        <textarea
                            className="chatbot-input"
                            placeholder="Escribe tu pregunta..."
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={handleKeyDown}
                            rows={1}
                            disabled={cargando}
                        />
                        <button
                            className="chatbot-enviar"
                            onClick={enviarMensaje}
                            disabled={cargando || !input.trim()}
                        >
                            ➤
                        </button>
                    </div>
                </div>
            )}

            <button
                className="chatbot-burbuja"
                onClick={() => setAbierto((prev) => !prev)}
                aria-label="Abrir chat"
            >
                {abierto ? '✕' : '🌸'}
            </button>
        </div>
    );
};

export default ChatbotWidget;