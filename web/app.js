const { useEffect, useState } = React;

const MENU = [
    { id: 1, name: 'Masala Dosa', price: 55, category: 'Meals', emoji: '🥞', description: 'Crisp dosa, potato masala and chutney', tag: 'Popular' },
    { id: 2, name: 'Paneer Frankie', price: 70, category: 'Quick bites', emoji: '🌯', description: 'Spiced paneer, onions and mint chutney', tag: 'Student pick' },
    { id: 3, name: 'Grilled Sandwich', price: 60, category: 'Quick bites', emoji: '🥪', description: 'Toasted bread, vegetables and cheese' },
    { id: 4, name: 'Chole Bhature', price: 85, category: 'Meals', emoji: '🍛', description: 'Chole with two fluffy bhaturas', tag: 'Popular' },
    { id: 5, name: 'Samosa', price: 25, category: 'Quick bites', emoji: '🔺', description: 'Golden potato and pea samosa' },
    { id: 6, name: 'Cold Coffee', price: 45, category: 'Drinks', emoji: '🥤', description: 'Chilled coffee with a creamy top' },
    { id: 7, name: 'Nimbu Paani', price: 30, category: 'Drinks', emoji: '🍋', description: 'Fresh lemon cooler with mint' },
    { id: 8, name: 'Gulab Jamun', price: 35, category: 'Desserts', emoji: '🍮', description: 'Warm, soft and soaked in syrup' }
];

const PICKUP_SLOTS = [
    '12:30 PM - 12:35 PM',
    '12:35 PM - 12:40 PM',
    '12:40 PM - 12:45 PM',
    '12:45 PM - 12:50 PM',
    '12:50 PM - 12:55 PM',
    '12:55 PM - 1:00 PM'
];

function money(amount) {
    return `₹${amount}`;
}

function App() {
    const [cart, setCart] = useState(() => JSON.parse(localStorage.getItem('oac-cart') || '{}'));
    const [category, setCategory] = useState('All');
    const [studentName, setStudentName] = useState('');
    const [pickupSlot, setPickupSlot] = useState(PICKUP_SLOTS[0]);
    const [error, setError] = useState('');
    const [confirmation, setConfirmation] = useState(null);

    useEffect(() => {
        localStorage.setItem('oac-cart', JSON.stringify(cart));
    }, [cart]);

    const categories = ['All', 'Meals', 'Quick bites', 'Drinks', 'Desserts'];
    const visibleMenu = category === 'All' ? MENU : MENU.filter(item => item.category === category);
    const cartItems = MENU.filter(item => cart[item.id]);
    const itemCount = cartItems.reduce((total, item) => total + cart[item.id], 0);
    const total = cartItems.reduce((sum, item) => sum + item.price * cart[item.id], 0);

    function changeQuantity(item, change) {
        setCart(current => {
            const next = { ...current };
            next[item.id] = (next[item.id] || 0) + change;
            if (next[item.id] <= 0) delete next[item.id];
            return next;
        });
    }

    function placeOrder(event) {
        event.preventDefault();
        if (!cartItems.length) {
            setError('Please add an item to your order first.');
            return;
        }
        if (!studentName.trim()) {
            setError('Please enter your name.');
            return;
        }

        const order = {
            number: `OAC-${Math.floor(1000 + Math.random() * 9000)}`,
            code: Math.floor(10 + Math.random() * 89),
            name: studentName.trim(),
            pickupSlot,
            total
        };
        const previousOrders = JSON.parse(localStorage.getItem('oac-orders') || '[]');
        localStorage.setItem('oac-orders', JSON.stringify([order, ...previousOrders]));
        setConfirmation(order);
        setCart({});
        setStudentName('');
        setError('');
    }

    function showCart() {
        document.getElementById('cart').scrollIntoView({ behavior: 'smooth' });
    }

    return (
        <div>
            <header className="topbar">
                <a href="#top" className="brand"><span className="logo">OA</span><span><strong>OPEN AIR</strong><small>CAFETERIA</small></span></a>
                <div className="top-actions"><span className="open-status">● OPEN NOW</span><button className="cart-button" onClick={showCart}>Cart <b>{itemCount}</b></button></div>
            </header>

            <main id="top">
                <section className="hero page-width">
                    <div>
                        <p className="eyebrow">COLLEGE CANTEEN · PRE-ORDER</p>
                        <h1>Skip the queue.<br /><span>Keep your break.</span></h1>
                        <p className="hero-text">Order your favourite campus food in advance and collect it when your break starts.</p>
                        <button className="main-button" onClick={() => document.getElementById('menu').scrollIntoView({ behavior: 'smooth' })}>Order your lunch <b>→</b></button>
                    </div>
                    <div className="status-card">
                        <div className="plate">🥗</div>
                        <div><p>Today's cafeteria status</p><strong>Serving fresh</strong><small>Average pickup · 15 min</small></div>
                    </div>
                </section>

                <section className="order-area page-width" id="menu">
                    <div className="menu-side">
                        <div className="section-title"><div><p className="eyebrow">TODAY'S MENU</p><h2>What are you craving?</h2></div><span className="veg-label">▣ All vegetarian</span></div>
                        <div className="categories">{categories.map(item => <button key={item} className={category === item ? 'selected' : ''} onClick={() => setCategory(item)}>{item}</button>)}</div>
                        <div className="menu-grid">{visibleMenu.map(item => <MenuCard key={item.id} item={item} onAdd={() => changeQuantity(item, 1)} />)}</div>
                    </div>
                    <Cart cartItems={cartItems} cart={cart} itemCount={itemCount} total={total} studentName={studentName} pickupSlot={pickupSlot} error={error} onChangeQuantity={changeQuantity} onNameChange={value => { setStudentName(value); setError(''); }} onSlotChange={value => { setPickupSlot(value); setError(''); }} onPlaceOrder={placeOrder} />
                </section>
            </main>

            <footer className="footer page-width"><span><strong>Open Air Cafeteria</strong> · made for a smoother campus break</span><span>Prototype project · pay at the counter</span></footer>
            {confirmation && <Confirmation order={confirmation} onClose={() => setConfirmation(null)} />}
        </div>
    );
}

function MenuCard({ item, onAdd }) {
    return <article className="food-card">
        <div className="food-picture"><span>{item.emoji}</span>{item.tag && <small>{item.tag}</small>}</div>
        <div className="food-info"><div className="food-heading"><h3>{item.name}</h3><i title="Vegetarian">▣</i></div><p>{item.description}</p><div className="food-bottom"><strong>{money(item.price)}</strong><button onClick={onAdd}>+ Add</button></div></div>
    </article>;
}

function Cart({ cartItems, cart, itemCount, total, studentName, pickupSlot, error, onChangeQuantity, onNameChange, onSlotChange, onPlaceOrder }) {
    return <aside className="cart" id="cart">
        <div className="cart-heading"><h2>Your order</h2><span>{itemCount} {itemCount === 1 ? 'item' : 'items'}</span></div>
        {!cartItems.length ? <div className="empty-cart"><div>🛒</div><h3>Your cart is empty</h3><p>Add something tasty from the menu to get started.</p></div> : <>
            <div className="cart-lines">{cartItems.map(item => <div className="cart-line" key={item.id}><span className="cart-emoji">{item.emoji}</span><div><strong>{item.name}</strong><small>{money(item.price)} each</small><div className="quantity"><button onClick={() => onChangeQuantity(item, -1)}>−</button><b>{cart[item.id]}</b><button onClick={() => onChangeQuantity(item, 1)}>+</button></div></div><strong>{money(item.price * cart[item.id])}</strong></div>)}</div>
            <div className="total-row"><span>Total</span><strong>{money(total)}</strong></div>
            <form onSubmit={onPlaceOrder}>
                <label>Your name</label><input value={studentName} onChange={event => onNameChange(event.target.value)} placeholder="Enter your name" />
                <label>Pickup time</label><select value={pickupSlot} onChange={event => onSlotChange(event.target.value)}>{PICKUP_SLOTS.map(slot => <option key={slot}>{slot}</option>)}</select>
                {error && <p className="error">{error}</p>}
                <button className="checkout" type="submit">Place pre-order · {money(total)}</button>
                <p className="pay-note">No online payment. Pay when you collect your food.</p>
            </form>
        </>}
    </aside>;
}

function Confirmation({ order, onClose }) {
    return <div className="modal" onClick={onClose}><div className="confirmation" onClick={event => event.stopPropagation()}><div className="success">✓</div><p className="eyebrow">ORDER CONFIRMED</p><h2>{order.number}</h2><p>Your order is booked for<br /><strong>{order.pickupSlot}</strong>.</p><div className="code">PICKUP CODE <b>{order.code}</b></div><button className="main-button" onClick={onClose}>Done</button></div></div>;
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
