import fetch from 'node-fetch';

// 👇 استبدل القيم بالـ Test Environment بتاعك
const PAYMOB_API_KEY = "ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmpiR0Z6Y3lJNklrMWxjbU5vWVc1MElpd2ljSEp2Wm1sc1pWOXdheUk2TVRFd05URTJNaXdpYm1GdFpTSTZJbWx1YVhScFlXd2lmUS5qWTN6UTNFZ3NFS2FaUVJ0ay1ULVdfTVUwckZOekhIcXBkSFJKN3NVRmh6T3F3Q0ZmVlJmUWotYjhtRGdrVlZFZUJ3UDhfUkhEWHp4bWJub09ROE9JZw==";
const PAYMOB_INTEGRATION_ID = "5396126"; // Integration ID للبطاقة
const IFRAME_ID = "978795"; // اكتب iframe ID هنا

async function testPayment() {
  try {
    // 1️⃣ Authentication Token
    const authRes = await fetch("https://accept.paymob.com/api/auth/tokens", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ api_key: PAYMOB_API_KEY })
    });
    const authData = await authRes.json();
    const authToken = authData.token;
    console.log("Auth Token:", authToken);

    // 2️⃣ Create Order
    const orderPayload = {
      auth_token: authToken,
      delivery_needed: "false",
      amount_cents: 1000, // 10 EGP
      currency: "EGP",
      merchant_order_id: `order_${Date.now()}`,
      items: [
        {
          name: "Wallet Deposit",
          amount_cents: 1000,
          description: "إيداع في المحفظة",
          quantity: 1
        }
      ]
    };
    const orderRes = await fetch("https://accept.paymob.com/api/ecommerce/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(orderPayload)
    });
    const orderData = await orderRes.json();
    const orderId = orderData.id;
    console.log("Order ID:", orderId);

    // 3️⃣ Payment Key
    const paymentKeyPayload = {
      auth_token: authToken,
      amount_cents: 1000,
      expiration: 3600,
      order_id: orderId,
      billing_data: {
        email: "test@example.com",
        first_name: "Abd",
        last_name: "Rahman",
        phone_number: "01000000000",
        apartment: "NA",
        floor: "NA",
        street: "NA",
        building: "NA",
        shipping_method: "NA",
        postal_code: "NA",
        city: "Cairo",
        country: "EG",
        state: "Cairo"
      },
      currency: "EGP",
      integration_id: PAYMOB_INTEGRATION_ID,
      lock_order_when_paid: "true",
      merchant_order_id: `user_123`
    };
    const paymentRes = await fetch("https://accept.paymob.com/api/acceptance/payment_keys", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(paymentKeyPayload)
    });
    const paymentData = await paymentRes.json();
    const paymentToken = paymentData.token;
    console.log("Payment Token:", paymentToken);

    // 4️⃣ Payment URL
    const paymentUrl = `https://accept.paymob.com/api/acceptance/iframes/${IFRAME_ID}?payment_token=${paymentToken}`;
    console.log("Payment URL:", paymentUrl);

  } catch (err) {
    console.error("Error:", err.message);
  }
}

testPayment();
