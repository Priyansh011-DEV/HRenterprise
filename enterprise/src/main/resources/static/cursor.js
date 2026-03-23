/* ========================
   CUSTOM CURSOR
======================== */

const dot  = document.createElement('div');
const ring = document.createElement('div');
dot.className  = 'cursor-dot';
ring.className = 'cursor-ring';
document.body.appendChild(dot);
document.body.appendChild(ring);

let mouseX = 0, mouseY = 0;
let ringX  = 0, ringY  = 0;

document.addEventListener('mousemove', e => {
    mouseX = e.clientX;
    mouseY = e.clientY;
    dot.style.left = mouseX + 'px';
    dot.style.top  = mouseY + 'px';
});

// Smooth ring follow
function animateRing() {
    ringX += (mouseX - ringX) * 0.12;
    ringY += (mouseY - ringY) * 0.12;
    ring.style.left = ringX + 'px';
    ring.style.top  = ringY + 'px';
    requestAnimationFrame(animateRing);
}
animateRing();

// Hover effect on interactive elements
document.addEventListener('mouseover', e => {
    if (e.target.matches('button, a, input, select, [onclick]')) {
        ring.classList.add('hovering');
    }
});

document.addEventListener('mouseout', e => {
    if (e.target.matches('button, a, input, select, [onclick]')) {
        ring.classList.remove('hovering');
    }
});