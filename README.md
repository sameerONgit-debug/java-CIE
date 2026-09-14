# Open Air Cafeteria

A simple prototype for a college canteen pre-order system. Students choose food, select a pickup time, and get an order number before going to the counter.

## Prototype features

- Menu cards for meals, quick bites, drinks, and desserts
- Category buttons
- Add items to a cart and change quantity
- Student name and pickup slot
- Order confirmation with order number and pickup code
- Data is saved in the browser with `localStorage`
- Responsive layout for mobile and desktop

There is deliberately no database, login, payment gateway, or large framework. This keeps the project suitable for a basic internal-exam prototype.

## Project structure

```text
src/com/openaircafeteria/
├── CanteenApp.java                 # Small Java OOP demonstration
└── model/
    ├── MenuItem.java               # Food item class
    ├── OrderLine.java              # Item + quantity class
    └── Order.java                  # Order and total calculation
web/
├── index.html                      # Page shell
├── styles.css                     # Styling and responsive layout
├── app.js                          # React menu, cart, and confirmation
└── vendor/                         # Local React and Babel browser files
```

## Run the web prototype

The frontend does not need a build tool. From the project root, either open `web/index.html` in a browser or run a small local static server:

```bash
python3 -m http.server 5500 --directory web
```

Then visit <http://localhost:5500>.

React, ReactDOM, and the small JSX transformer are stored locally in `web/vendor/`, so the preview no longer depends on an external CDN.

## Run the Java OOP demo

Java 8 or newer is enough:

```bash
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out com.openaircafeteria.CanteenApp
```

The console demo creates a `MenuItem`, adds items to an `Order`, and prints the calculated total.

## OOP points for the viva

- `MenuItem` stores the name, price, category, and emoji of food.
- `OrderLine` connects one item with a quantity.
- `Order` stores multiple order lines and calculates the total.
- Private fields, constructors, getters, and methods demonstrate encapsulation.
- `CanteenApp` creates and uses the objects.
