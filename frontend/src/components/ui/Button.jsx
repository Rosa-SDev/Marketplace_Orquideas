import './Button.css';

const Button = ({text, onClick, type = "button"}) =>{
    return(
        <button className="btn-primary" onClick = {onClick} type={type}>
            {text}
        </button>
    );
};

export default Button;