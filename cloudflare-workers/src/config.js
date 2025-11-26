// ════════════════════════════════════════════════════════════
// 🔑 Configuration
// ════════════════════════════════════════════════════════════

export const getConfig = (env) => ({
  paymob: {
    apiKey: env.PAYMOB_API_KEY,
    integrationId: env.PAYMOB_INTEGRATION_ID,
    iframeId: env.PAYMOB_IFRAME_ID,
    hmacSecret: env.PAYMOB_HMAC_SECRET,
    baseUrl: 'https://accept.paymob.com/api'
  },
  firebase: {
    databaseUrl: env.FIREBASE_DATABASE_URL,
    serviceAccount: JSON.parse(env.FIREBASE_SERVICE_ACCOUNT_KEY)
  }
});

// CORS Headers
export const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
};

// JSON Response Helper
export const jsonResponse = (data, status = 200) => {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'Content-Type': 'application/json',
      ...corsHeaders
    }
  });
};

// Error Response Helper
export const errorResponse = (message, status = 400) => {
  return jsonResponse({ error: message }, status);
};