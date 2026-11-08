import { bootPortal } from './app.ts';
import './styles/app.css';
import './styles/preview-theme.css';

// Boot the full VTC Production Portal
bootPortal();

// React Bridge container for auxiliary modals
const bridgeEl = document.createElement('div');
bridgeEl.id = 'vtc-react-bridge';
bridgeEl.hidden = true;
document.body.appendChild(bridgeEl);
