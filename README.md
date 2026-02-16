# 📚 Review: Tu Guía de Estudio Personalizada

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2021+-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20+%20Clean-blue)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Review** es una aplicación móvil para Android diseñada para transformar la toma de apuntes en un sistema de estudio dinámico y estructurado. A diferencia de las apps rígidas de idiomas o materias específicas, **Review permite estudiar cualquier contenido**, organizándolo en carpetas y guías totalmente personalizables.

Funciona como un híbrido entre un bloc de notas de alto nivel y un sistema de repaso por tarjetas (*flashcards*).

---

## 📑 Índice

- [✨ Características Principales](#-características-principales)
- [📱 Vista de la Aplicación](#-vista-de-la-aplicación)
- [🏗️ Arquitectura y Diseño](#️-arquitectura-y-diseño)
- [🛠️ Stack Tecnológico](#️-stack-tecnológico)
- [🧪 Calidad de Software (Testing)](#-calidad-de-software-testing)
- [🚀 Instalación](#-instalación)
- [🧠 Aprendizajes Clave](#-aprendizajes-clave)
- [👤 Autor](#-autor)
- [📩 Contacto y Colaboración](#-contacto-y-colaboración)

---

## ✨ Características Principales

* **📁 Organización Jerárquica:** Creación de carpetas personalizadas con iconos y colores únicos.
* **🗂️ Guías de Estudio:** Estructura tus temas con preguntas ilimitadas dentro de cada guía.
* **📝 Editor Enriquecido:** Agrega texto con colores personalizados e integra imágenes en tus apuntes.
* **🌙 Experiencia Nativa:** Modo oscuro por defecto y diseño basado en Material Design.
* **💾 Privacidad Total:** Persistencia 100% local en el dispositivo (funciona sin conexión).

---

## 📱 Vista de la Aplicación

<p align="left">
  <img src="assets/readme/Samsung Galaxy Tab S7 Screenshot 1.png" width="250"/>
  <img src="assets/readme/Samsung Galaxy Tab S7 Screenshot 2.png" width="250"/>
  <img src="assets/readme/Samsung Galaxy Tab S7 Screenshot 3.png" width="250"/>
</p>

<p align="left">
  <img src="assets/readme/Samsung Galaxy Tab S7 Screenshot 4.png" width="250"/>
  <img src="assets/readme/Samsung Galaxy Tab S7 Screenshot 5.png" width="250"/>
  <img src="assets/readme/Samsung Galaxy Tab S7 Screenshot 6.png" width="250"/>
</p>

<p align="left">
   <img src="assets/readme/Samsung Galaxy Tab S7 Screenshot 7.png" width="250"/>
</p>

---

## 🏗️ Arquitectura y Diseño

El proyecto implementa **Clean Architecture** bajo el patrón **MVVM**, garantizando un código desacoplado, testeable y fácil de escalar.

### Capas del Sistema

| Capa | Responsabilidad | Tecnologías clave |
| :--- | :--- | :--- |
| **Presentation** | Gestión de UI, Estados de vista y navegación. | ViewModels, LiveData, XML Layouts. |
| **Domain** | Reglas de negocio puras (Single Source of Truth). | Use Cases, Domain Models, Interfaces. |
| **Data** | Implementación de repositorios y fuentes de datos. | DataStore, File System (JSON/XML). |

> **Flujo de Dependencias:** Presentation → Domain ← Data. La capa de **Domain** es el núcleo y no depende de ninguna otra, facilitando su mantenimiento.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** [Kotlin](https://kotlinlang.org/) + Coroutines (Asincronía).
* **DI:** [Dagger Hilt](https://dagger.dev/hilt/) para la inyección de dependencias.
* **UI:** Material Components (1.5.0) y XML dinámicos.
* **Persistencia Híbrida:** * **DataStore:** Para configuraciones y preferencias.
    * **JSON & XML:** Motores de persistencia custom para una estructura de datos flexible.
* **Testing:** Unit Testing con **JUnit** y **MockK** para la validación de la lógica de negocio.

---

## 🧪 Calidad de Software (Testing)

El proyecto pone especial énfasis en la estabilidad de la capa de dominio:
* Validación de **Casos de Uso**.
* Pruebas de **Lógica de Negocio**.
* Simulación de interacciones con repositorios mediante **Mocks**.

---

## 🚀 Instalación

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/Jonathan1429/Review.git
    ```
2.  **Abrir en Android Studio:** Importa el proyecto y espera a que Gradle sincronice las dependencias.
3.  **Ejecutar:** Conecta un dispositivo físico o emulador con **API 21** o superior.

---

## 🧠 Aprendizajes Clave

Este proyecto marcó un hito en mi formación como desarrollador, permitiéndome dominar:

* **Diseño de Arquitectura:** La transición de un código monolítico a una estructura escalable (Clean Architecture).
* **Desacoplamiento:** Cómo separar la lógica de negocio de los detalles de implementación (UI y persistencia).
* **Persistencia Custom:** Gestión eficiente de archivos locales sin depender de bases de datos tradicionales como Room, optimizando el acceso a datos JSON/XML.

---

## 👤 Autor

**José Jonathan Escobar Vázquez** *Ingeniero en Sistemas Computacionales | Desarrollador Android Kotlin*

---

## 📩 Contacto y Colaboración
Estoy en búsqueda activa de oportunidades como **Android Developer**. Si te interesa mi perfil o quieres charlar sobre arquitectura móvil, ¡no dudes en contactarme!

[📩 Enviar Correo](mailto:jona142959@gmail.com) | [💼 LinkedIn](https://www.linkedin.com/in/jonathan-escobar-5315a0206/) | [GitHub](https://github.com/Jonathan1429)

---
