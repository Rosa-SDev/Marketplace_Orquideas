# React + Vite

Esta plantilla ofrece una configuración mínima para que React funcione en Vite con HMR y algunas reglas de ESLint.

Actualmente, hay dos plugins oficiales disponibles:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) utiliza [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) utiliza [SWC](https://swc.rs/)

## Compilador de React

El compilador de React no está habilitado en esta plantilla debido a su impacto en el rendimiento del desarrollo y la compilación. Para añadirlo, consulta [esta documentación](https://react.dev/learn/react-compiler/installation).

## Ampliación de la configuración de ESLint

Si estás desarrollando una aplicación de producción, te recomendamos utilizar TypeScript con las reglas de linting sensibles a los tipos habilitadas. Consulte la [plantilla TS](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) para obtener información sobre cómo integrar TypeScript y [`typescript-eslint`](https://typescript-eslint.io) en su proyecto.
