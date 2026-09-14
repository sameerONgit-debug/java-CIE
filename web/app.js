const { useEffect, useMemo, useState } = React;

const FALLBACK_MENU = [
    { id: 'masala-dosa', name: 'Masala Dosa', description: 'Crisp dosa with potato masala and chutney', price: 55, category: 'Meals', emoji: '🥞', vegetarian: true, popular: true },
    { id: 'paneer-roll', name: 'Paneer Frankie', description: 'Spiced paneer, onions and mint chutney', price: 70, category: 'Quick bites', emoji: '🌯', vegetarian: true, popular: true },
    { id: 'veg-sandwich', name: 'Grilled Sandwich', description: 'Toasted bread, vegetables and cheese', price: 60, category: 'Quick bites', emoji: '🥪', vegetarian: true, popular: false },
    { id: 'chole-bhature', name: 'Chole Bhature', description: 'Comforting chole with two fluffy bhaturas', price: 85, category: 'Meals', emoji: '🍛', vegetarian: true, popular: true },
    { id: 'samosa', name: 'Samosa', description: 'Golden potato and pea samosa', price: 25, category: 'Quick bites', emoji: '🔺', vegetarian: true, popular: false },
    { id: 'cold-coffee', name: 'Cold Coffee', description: 'Chilled coffee topped with a creamy cloud', price: 45, category: 'Drinks', emoji: '🥤', vegetarian: true, popular: true },
    { id: 'nimbu-paani', name: 'Nimbu Paani', description: 'Fresh lemon cooler with a pinch of mint', price: 30, category: 'Drinks', emoji: '🍋', vegetarian: true, popular: false },
    { id: 'gulab-jamun', name: 'Gulab Jamun', description: 'Soft syrup-soaked dessert, served warm', price: 35, category: 'Desserts', emoji: '🍮', vegetarian: true, popular: false }
];

const FALLBACK_SLOTS = [
    '12:30 PM - 12:45 PM',
    '12:45 PM - 1:00 PM',
    '1:00 PM - 1:15 PM',
    '1:15 PM - 1:30 PM'
];

function money(value) {
    return `₹${Number(value).toLocaleString('en-IN')}`;
}

function App() {
    const [menu, setMenu] = useState(FALLBACK_MENU);
    const [slots, setSlots] = useState(FALLBACK_SLOTS);
    const [cart, setCart] = useState(() => {
        try {
            return JSON.parse(localStorage.getItem('open-air-cart')) || {};
        } catch (error) {
            return {};
        }
    });
    const [category, setCategory] = useState('All');
    const [search, setSearch] = useState('');
    const [demoMode, setDemoMode] = useState(true);
    const [error, setError] = useState('');
    const [notice, setNotice] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [confirmation, setConfirmation] = useState(null);
    const [form, setForm] = useState({
        name: '',
        email: '',
        pickupSlot: FALLBACK_SLOTS[0],
        notes: ''
    });

    useEffect(() => {
        fetch('/api/menu')
            .then(response => {
                if (!response.ok) throw new Error('API unavailable');
                return response.json();
            })
            .then(data => {
                if (Array.isArray(data.menu)) setMenu(data.menu);
                if (Array.isArray(data.pickupSlots) && data.pickupSlots.length) {
                    setSlots(data.pickupSlots);
                    setForm(current => ({ ...current, pickupSlot: current.pickupSlot || data.pickupSlots[0] }));
                }
                setDemoMode(false);
            })
            .catch(() => setDemoMode(true));
    }, []);

    useEffect(() => {
        localStorage.setItem('open-air-cart', JSON.stringify(cart));
    }, [cart]);

    useEffect(() => {
        if (!notice) return undefined;
        const timer = setTimeout(() => setNotice(''), 3000);
        return () => clearTimeout(timer);
    }, [notice]);

    const categories = useMemo(() => ['All', ...new Set(menu.map(item => item.category))], [menu]);
    const filteredMenu = useMemo(() => {
        const query = search.trim().toLowerCase();
        return menu.filter(item => {
            const matchesCategory = category === 'All' || item.category === category;
            const matchesSearch = !query || `${item.name} ${item.description}`.toLowerCase().includes(query);
            return matchesCategory && matchesSearch;
        });
    }, [menu, category, search]);

    const cartItems = useMemo(() => Object.entries(cart)
        .map(([id, quantity]) => {
            const item = menu.find(menuItem => menuItem.id === id);
            return item ? { ...item, quantity } : null;
        })
        .filter(Boolean), [cart, menu]);
    const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    const total = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

    function addItem(item) {
        setCart(current => ({ ...current, [item.id]: (current[item.id] || 0) + 1 }));
        setNotice(`${item.name} added to your order`);
    }

    function changeQuantity(id, amount) {
        setCart(current => {
            const next = { ...current, [id]: (current[id] || 0) + amount };
            if (next[id] <= 0) delete next[id];
            return next;
        });
    }

    function updateForm(event) {
        const { name, value } = event.target;
        setForm(current => ({ ...current, [name]: value }));
        setError('');
    }

    function scrollToMenu() {
        document.getElementById('menu-section').scrollIntoView({ behavior: 'smooth' });
    }

    function scrollToCart() {
        document.getElementById('cart-section').scrollIntoView({ behavior: 'smooth' });
    }

    async function placeOrder(event) {
        event.preventDefault();
        setError('');
        if (!cartItems.length) {
            setError('Add at least one item before placing your order.');
            return;
        }
        if (!form.name.trim() || !form.email.trim() || !form.pickupSlot) {
            setError('Please fill in your name, email, and pickup slot.');
            return;
        }

        setIsSubmitting(true);
        const payload = {
            name: form.name,
            email: form.email,
            pickupSlot: form.pickupSlot,
            notes: form.notes,
            items: cartItems.map(item => ({ id: item.id, quantity: item.quantity }))
        };

        try {
            const response = await fetch('/api/orders', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.error || 'Could not place the order');
            setConfirmation(result.order);
            setDemoMode(false);
        } catch (requestError) {
            // This lets the UI be demonstrated by opening web/index.html directly.
            // When the Java server is running, validation errors are shown instead.
            if (requestError.name !== 'TypeError'
                && requestError.name !== 'SyntaxError'
                && requestError.message !== 'Failed to fetch') {
                setError(requestError.message);
                setIsSubmitting(false);
                return;
            }
            setConfirmation({
                orderNumber: `OAC-DEMO-${Math.floor(100 + Math.random() * 899)}`,
                pickupCode: '42A',
                pickupSlot: form.pickupSlot,
                total,
                status: 'confirmed'
            });
            setNotice('Demo order saved in this browser');
        }

        setCart({});
        setForm(current => ({ ...current, notes: '' }));
        setIsSubmitting(false);
    }

    return (
        <>
            <header className="topbar">
                <a href="#top" className="brand">
                    <span className="brand-mark">OA</span>
                    <span className="brand-name">OPEN AIR<small>CAFETERIA</small></span>
                </a>
                <div className="topbar-actions">
                    <span className="open-label">Open now · 10:00 AM – 5:00 PM</span>
                    <button className="cart-top-button" onClick={scrollToCart}>
                        Your order <span>{totalItems ? `(${totalItems})` : '·'}</span>
                    </button>
                </div>
            </header>

            <main className="page-wrap" id="top">
                <section className="hero">
                    <div className="hero-copy">
                        <p className="eyebrow">SMART CAMPUS EATING</p>
                        <h1>Good food.<br /><em>No queue.</em></h1>
                        <p>Pre-order your favourite cafeteria bites, choose a pickup time, and spend your break with friends — not in a line.</p>
                        <button className="primary-button" onClick={scrollToMenu}>Start an order <span>→</span></button>
                        <span className="hero-note">Freshly prepared on campus</span>
                    </div>
                    <div className="hero-card">
                        <div className="hero-card-top"><span>Today's pulse</span><strong>● LIVE</strong></div>
                        <div className="wait-time">
                            <div className="wait-icon">⏱</div>
                            <div><strong>12 min</strong><span>average pickup time</span></div>
                        </div>
                        <div className="hero-card-foot"><span>Lunch rush status</span><b>Comfortable</b></div>
                    </div>
                </section>

                <section id="menu-section">
                    <div className="section-head">
                        <div><span className="section-kicker">THE MENU</span><h2>Made fresh. Picked up fast.</h2></div>
                        <label className="search-box"><span>⌕</span><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Search the menu" /></label>
                    </div>
                    <div className="menu-and-cart">
                        <div>
                            <div className="filters">
                                {categories.map(itemCategory => (
                                    <button key={itemCategory} className={`filter-button ${category === itemCategory ? 'active' : ''}`} onClick={() => setCategory(itemCategory)}>{itemCategory}</button>
                                ))}
                            </div>
                            <div className="menu-grid">
                                {filteredMenu.map(item => <MenuCard key={item.id} item={item} onAdd={addItem} />)}
                                {!filteredMenu.length && <div className="empty-menu">No menu item matches that search.</div>}
                            </div>
                        </div>
                        <CartPanel
                            cartItems={cartItems}
                            totalItems={totalItems}
                            total={total}
                            slots={slots}
                            form={form}
                            error={error}
                            isSubmitting={isSubmitting}
                            demoMode={demoMode}
                            onQuantityChange={changeQuantity}
                            onFormChange={updateForm}
                            onSubmit={placeOrder}
                        />
                    </div>
                </section>

                <section className="how-it-works">
                    <div>
                        <span className="section-kicker">HOW IT WORKS</span>
                        <h2>Your break,<br />back in your hands.</h2>
                        <p>One small pre-order helps the cafeteria keep the crowd moving during busy college breaks.</p>
                    </div>
                    <div className="steps">
                        <div className="step"><div className="step-number">01</div><h3>Pick your food</h3><p>Choose from today's simple, student-friendly menu.</p></div>
                        <div className="step"><div className="step-number">02</div><h3>Choose a slot</h3><p>Reserve a fifteen-minute pickup window that suits you.</p></div>
                        <div className="step"><div className="step-number">03</div><h3>Collect & enjoy</h3><p>Show your order number at the pickup counter.</p></div>
                    </div>
                </section>

                <footer className="footer"><span><strong>Open Air Cafeteria</strong> · College canteen pre-order prototype</span><span>Built for a smoother campus break</span></footer>
            </main>

            {confirmation && <Confirmation order={confirmation} onClose={() => setConfirmation(null)} />}
            {notice && <div className="toast">{notice}</div>}
        </>
    );
}

function MenuCard({ item, onAdd }) {
    return (
        <article className="menu-card">
            <div className="item-visual">
                {item.popular && <span className="popular-tag">Student favourite</span>}
                <span className="item-emoji">{item.emoji}</span>
            </div>
            <div className="item-info">
                <div className="item-title-line"><h3>{item.name}</h3>{item.vegetarian && <span className="veg-dot" title="Vegetarian" />}</div>
                <p className="item-description">{item.description}</p>
                <div className="item-bottom"><span className="item-price">{money(item.price)} <small>each</small></span><button className="add-button" onClick={() => onAdd(item)}>+ Add</button></div>
            </div>
        </article>
    );
}

function CartPanel({ cartItems, totalItems, total, slots, form, error, isSubmitting, demoMode, onQuantityChange, onFormChange, onSubmit }) {
    return (
        <aside className="cart-card" id="cart-section">
            <div className="cart-header"><h2>Your order</h2><span className="cart-count">{totalItems || 0}</span></div>
            {!cartItems.length ? (
                <div className="empty-cart"><div className="empty-cart-icon">✦</div><p>Your order is empty.<br />Add something delicious from the menu.</p></div>
            ) : (
                <>
                    <div>{cartItems.map(item => (
                        <div className="cart-line" key={item.id}>
                            <span className="cart-line-emoji">{item.emoji}</span>
                            <div className="cart-line-info"><strong>{item.name}</strong><small>{money(item.price)} each</small><div className="quantity"><button onClick={() => onQuantityChange(item.id, -1)}>−</button><span>{item.quantity}</span><button onClick={() => onQuantityChange(item.id, 1)}>+</button></div></div>
                            <span className="cart-line-total">{money(item.price * item.quantity)}</span>
                        </div>
                    ))}</div>
                    <div className="cart-total"><span>Total to pay</span><strong>{money(total)}</strong></div>
                    <form className="cart-form" onSubmit={onSubmit}>
                        <label className="form-label" htmlFor="student-name">Your name</label>
                        <input id="student-name" name="name" className="form-input" value={form.name} onChange={onFormChange} placeholder="e.g. Aditi Sharma" />
                        <label className="form-label" htmlFor="student-email">College email</label>
                        <input id="student-email" name="email" type="email" className="form-input" value={form.email} onChange={onFormChange} placeholder="you@college.edu" />
                        <label className="form-label" htmlFor="pickup-slot">Pickup slot</label>
                        <select id="pickup-slot" name="pickupSlot" className="form-select" value={form.pickupSlot} onChange={onFormChange}>{slots.map(slot => <option key={slot} value={slot}>{slot}</option>)}</select>
                        <label className="form-label" htmlFor="order-note">Note <span>(optional)</span></label>
                        <textarea id="order-note" name="notes" className="form-textarea" value={form.notes} onChange={onFormChange} placeholder="Less spicy, extra chutney..." />
                        {error && <p className="form-error">{error}</p>}
                        <button className="place-button" disabled={isSubmitting} type="submit">{isSubmitting ? 'Placing your order…' : `Place order · ${money(total)}`}</button>
                        <p className="demo-note">{demoMode ? 'Prototype mode · orders stay in this browser if the Java server is off' : 'No extra charges · pay at the cafeteria counter'}</p>
                    </form>
                </>
            )}
        </aside>
    );
}

function Confirmation({ order, onClose }) {
    return (
        <div className="modal-backdrop" onClick={onClose}>
            <div className="success-modal" onClick={event => event.stopPropagation()}>
                <div className="success-check">✓</div>
                <span className="section-kicker">ORDER CONFIRMED</span>
                <h2>{order.orderNumber}</h2>
                <p className="success-copy">Your food will be ready for pickup during<br /><strong>{order.pickupSlot}</strong>.</p>
                <div className="pickup-code">PICKUP CODE · {order.pickupCode}</div>
                <button className="primary-button modal-close" onClick={onClose}>Done — see you at the counter</button>
            </div>
        </div>
    );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
