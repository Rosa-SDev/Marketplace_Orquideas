import { useState, useRef, useEffect } from 'react';
import api from '../../services/api';
import ReactMarkdown from 'react-markdown';
import './ChatbotWidget.css';

const ChatbotWidget = () => {
    const [abierto, setAbierto] = useState(false);
    const [hover, setHover] = useState(false);
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

        const nuevosMensajes = [...mensajes, { rol: 'usuario', texto }];
        setMensajes(nuevosMensajes);
        setInput('');
        setCargando(true);

        try {
            const res = await api.post('/chatbot', {
                mensaje: texto,
                historial: mensajes.map(m => ({
                    rol: m.rol,
                    texto: m.texto
                }))
            });
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
            {/* Ventana de chat */}
            {abierto && (
                <div className="chatbot-ventana">
                    <div className="chatbot-header">
                        <span>🌸 Asistente Orquídeas</span>
                        <button className="chatbot-cerrar" onClick={() => setAbierto(false)}>✕</button>
                    </div>

                    <div className="chatbot-mensajes">
                        {mensajes.map((msg, i) => (
                            <div key={i} className={`chatbot-mensaje chatbot-mensaje--${msg.rol}`}>
                                {msg.rol === 'bot'
                                    ? <ReactMarkdown>{msg.texto}</ReactMarkdown>
                                    : msg.texto
                                }
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

            {/* Tooltip al hacer pasar el cursor por el widget */}
            {!abierto && hover && (
                <div className="chatbot-tooltip">
                    ¡Hola! 🌸 ¿En qué puedo ayudarte?
                </div>
            )}

            {/* Burbuja flotante */}
            <button
                className="chatbot-burbuja"
                onClick={() => setAbierto((prev) => !prev)}
                onMouseEnter={() => setHover(true)}
                onMouseLeave={() => setHover(false)}
                aria-label="Abrir chat"
            >
                {abierto ? '✕' : '🌸'}
                {!abierto && <span className="chatbot-badge">!</span>}
            </button>
        </div>
    );
};

export default ChatbotWidget;