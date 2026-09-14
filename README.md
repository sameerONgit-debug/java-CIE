# Open Air Cafeteria

A small college canteen pre-ordering prototype for **Open Air Cafeteria**. Students can select food, choose a pickup slot, and show an order number at the counter instead of waiting in a long queue.

## What this prototype uses

- **Basic Java OOP** for menu items, orders, order lines, and the small service layer
- **HTML and CSS** for the page structure and responsive design
- **React 18 + JavaScript** for the interactive menu and cart
- The JDK's built-in `HttpServer` only — no Spring, database, npm, or other framework

The Java service stores orders in memory. That is intentional: it keeps the project small and easy to explain for an internal exam. Restarting the server clears the orders.

## What works

- Browse and search today's menu
- Filter by meals, quick bites, drinks, or desserts
- Add items to a cart and change quantities
- Enter name, college email, and pickup time
- Place an order and receive an order number plus pickup code
- Responsive design for a laptop or phone
- Demo mode: the React page still works with sample data if the Java server is not running

## Folder structure

```text
src/com/openaircafeteria/
├── CanteenServer.java              # Small JDK server and API routes
├── model/
│   ├── MenuItem.java               # Menu item class
│   ├── OrderLine.java              # Item + quantity class
│   └── Order.java                  # Order class and total calculation
└── service/
    ├── CanteenService.java         # Menu, validation, and in-memory orders
    └── JsonUtil.java               # Tiny JSON helper; no external library
web/
├── index.html
├── styles.css
└── app.js                          # React UI
```

## Run it

Java 11 or newer is recommended. From the project root:

```bash
mkdir -p out
javac --add-modules jdk.httpserver -d out $(find src -name '*.java')
java --add-modules jdk.httpserver -cp out com.openaircafeteria.CanteenServer
```

Now open **http://localhost:8080**.

To use another port:

```bash
java --add-modules jdk.httpserver -cp out com.openaircafeteria.CanteenServer 9090
```

On Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac --add-modules jdk.httpserver -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java --add-modules jdk.httpserver -cp out com.openaircafeteria.CanteenServer
```

React is loaded from a CDN in `web/index.html`, so the browser needs internet access for the React scripts. If the Java server is unavailable, the page uses demo menu data and keeps the prototype order in browser memory/local storage.

## API routes

| Method | Route | Purpose |
| --- | --- | --- |
| `GET` | `/api/menu` | Returns the menu and pickup slots |
| `GET` | `/api/orders` | Returns orders currently held in memory |
| `POST` | `/api/orders` | Validates and creates one order |

## Simple OOP explanation for the viva

- `MenuItem` represents one food item with properties such as name, price, and category.
- `OrderLine` represents one menu item and its quantity.
- `Order` owns its order lines and calculates the subtotal and total.
- `CanteenService` keeps business logic such as validating the form and finding menu items.
- `CanteenServer` only handles HTTP requests and passes the data to `CanteenService`.
- Private fields, constructors, getters, and methods demonstrate encapsulation.
