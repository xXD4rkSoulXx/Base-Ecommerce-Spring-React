import { deflateSync } from 'node:zlib'

const WIDTH = 900
const HEIGHT = 1125
const SS = 2

const PALETTE = {
  BLACK: '#25252d',
  BLUE: '#2f5bff',
  CYAN: '#00d9ff',
  GOLD: '#d4a72c',
  GRAY: '#6d7280',
  GREEN: '#1f9d55',
  LIME: '#b6ff2e',
  MAGENTA: '#ff1fa9',
  MAROON: '#7a1530',
  OLIVE: '#6b7a2f',
  ORANGE: '#ff7a1a',
  PINK: '#ff7fbf',
  PURPLE: '#7a3cff',
  RED: '#e5233a',
  SALMON: '#ff8a73',
  TEAL: '#0f9d9a',
  WHITE: '#e9ebf2',
  YELLOW: '#ffe033'
}

const ACCENTS = ['#00d9ff', '#ff1fa9', '#b6ff2e']

const hex = (value) => [1, 3, 5].map((i) => parseInt(value.slice(i, i + 2), 16))
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t))
const darken = (rgb, t) => mix(rgb, [0, 0, 0], t)
const lighten = (rgb, t) => mix(rgb, [255, 255, 255], t)

class Canvas {
  constructor() {
    this.w = WIDTH * SS
    this.h = HEIGHT * SS
    this.px = new Uint8Array(this.w * this.h * 3)
    this.ty = 0
  }

  background(accent) {
    const top = hex('#0b0b11')
    const bottom = hex('#17171f')
    for (let y = 0; y < this.h; y++) {
      const color = mix(top, bottom, y / this.h)
      for (let x = 0; x < this.w; x++) {
        const i = (y * this.w + x) * 3
        this.px[i] = color[0]
        this.px[i + 1] = color[1]
        this.px[i + 2] = color[2]
      }
    }
    this.ellipse(450, 560, 420, 470, accent, 0.07)
    this.ellipse(450, 560, 300, 340, accent, 0.07)
    for (let x = 90; x < WIDTH; x += 90) {
      this.line(x, 0, x, HEIGHT, 1, [255, 255, 255], 0.035)
    }
    for (let y = 90; y < HEIGHT; y += 90) {
      this.line(0, y, WIDTH, y, 1, [255, 255, 255], 0.035)
    }
    this.line(60, 60, 220, 60, 5, accent, 0.9)
    this.line(60, 60, 60, 160, 5, accent, 0.9)
    this.line(840, HEIGHT - 60, 680, HEIGHT - 60, 5, accent, 0.9)
    this.line(840, HEIGHT - 60, 840, HEIGHT - 160, 5, accent, 0.9)
  }

  blend(x, y, rgb, alpha) {
    if (x < 0 || y < 0 || x >= this.w || y >= this.h) {
      return
    }
    const i = (y * this.w + x) * 3
    if (alpha >= 1) {
      this.px[i] = rgb[0]
      this.px[i + 1] = rgb[1]
      this.px[i + 2] = rgb[2]
      return
    }
    this.px[i] += (rgb[0] - this.px[i]) * alpha
    this.px[i + 1] += (rgb[1] - this.px[i + 1]) * alpha
    this.px[i + 2] += (rgb[2] - this.px[i + 2]) * alpha
  }

  poly(points, rgb, alpha = 1) {
    const pts = points.map(([x, y]) => [x * SS, (y + this.ty) * SS])
    let minY = Infinity
    let maxY = -Infinity
    for (const [, y] of pts) {
      minY = Math.min(minY, y)
      maxY = Math.max(maxY, y)
    }
    const from = Math.max(0, Math.floor(minY))
    const to = Math.min(this.h - 1, Math.ceil(maxY))
    for (let y = from; y <= to; y++) {
      const scan = y + 0.5
      const hits = []
      for (let i = 0; i < pts.length; i++) {
        const [x1, y1] = pts[i]
        const [x2, y2] = pts[(i + 1) % pts.length]
        if ((y1 <= scan && y2 > scan) || (y2 <= scan && y1 > scan)) {
          hits.push(x1 + ((scan - y1) / (y2 - y1)) * (x2 - x1))
        }
      }
      hits.sort((a, b) => a - b)
      for (let i = 0; i + 1 < hits.length; i += 2) {
        const start = Math.max(0, Math.round(hits[i]))
        const end = Math.min(this.w - 1, Math.round(hits[i + 1]) - 1)
        for (let x = start; x <= end; x++) {
          this.blend(x, y, rgb, alpha)
        }
      }
    }
  }

  ellipse(cx, cy, rx, ry, rgb, alpha = 1) {
    this.poly(arc(cx, cy, rx, ry, 0, 360, 64), rgb, alpha)
  }

  rrect(x, y, w, h, r, rgb, alpha = 1) {
    const radius = Math.min(r, w / 2, h / 2)
    const points = [
      ...arc(x + w - radius, y + radius, radius, radius, -90, 0, 8),
      ...arc(x + w - radius, y + h - radius, radius, radius, 0, 90, 8),
      ...arc(x + radius, y + h - radius, radius, radius, 90, 180, 8),
      ...arc(x + radius, y + radius, radius, radius, 180, 270, 8)
    ]
    this.poly(points, rgb, alpha)
  }

  line(x1, y1, x2, y2, thickness, rgb, alpha = 1) {
    const dx = x2 - x1
    const dy = y2 - y1
    const length = Math.hypot(dx, dy) || 1
    const nx = (-dy / length) * (thickness / 2)
    const ny = (dx / length) * (thickness / 2)
    this.poly(
      [
        [x1 + nx, y1 + ny],
        [x2 + nx, y2 + ny],
        [x2 - nx, y2 - ny],
        [x1 - nx, y1 - ny]
      ],
      rgb,
      alpha
    )
  }

  stroke(points, thickness, rgb, alpha = 1) {
    for (let i = 0; i < points.length - 1; i++) {
      this.line(points[i][0], points[i][1], points[i + 1][0], points[i + 1][1], thickness, rgb, alpha)
    }
  }

  downsample() {
    const out = Buffer.alloc(WIDTH * HEIGHT * 3)
    for (let y = 0; y < HEIGHT; y++) {
      for (let x = 0; x < WIDTH; x++) {
        for (let c = 0; c < 3; c++) {
          let sum = 0
          for (let dy = 0; dy < SS; dy++) {
            for (let dx = 0; dx < SS; dx++) {
              sum += this.px[((y * SS + dy) * this.w + (x * SS + dx)) * 3 + c]
            }
          }
          out[(y * WIDTH + x) * 3 + c] = Math.round(sum / (SS * SS))
        }
      }
    }
    return out
  }
}

function arc(cx, cy, rx, ry, from, to, steps) {
  const points = []
  for (let i = 0; i <= steps; i++) {
    const angle = ((from + ((to - from) * i) / steps) * Math.PI) / 180
    points.push([cx + rx * Math.cos(angle), cy + ry * Math.sin(angle)])
  }
  return points
}

const mirror = (right) => [...right, ...right.map(([x, y]) => [WIDTH - x, y]).reverse()]
const mirrorPoint = ([x, y]) => [WIDTH - x, y]

function rim(c, points) {
  c.stroke([...points, points[0]], 3, [255, 255, 255], 0.16)
}

function body(c, col, points) {
  c.poly(points, col.base)
  rim(c, points)
}

function shadeRight(c, x1, x2, y1, y2) {
  c.poly(
    [
      [x1, y1],
      [x2, y1],
      [x2, y2],
      [x1, y2]
    ],
    [0, 0, 0],
    0.13
  )
}

function reflective(c, x1, x2, y, h) {
  c.poly(
    [
      [x1, y],
      [x2, y],
      [x2, y + h],
      [x1, y + h]
    ],
    [214, 220, 232],
    0.7
  )
  c.line(x1, y + h * 0.5, x2, y + h * 0.5, 2, [255, 255, 255], 0.5)
}

function collar(c, col, y) {
  c.ellipse(450, y + 4, 70, 42, col.dark)
  c.ellipse(450, y - 2, 52, 28, darken(col.dark, 0.45))
}

function zipper(c, col, y1, y2) {
  c.line(450, y1, 450, y2, 6, col.light, 0.9)
  c.rrect(438, y1 + 6, 24, 40, 6, col.accent)
}

const DRAW = {
  tee(c, col) {
    const pts = mirror([
      [450, 205],
      [510, 205],
      [615, 235],
      [745, 345],
      [690, 425],
      [615, 380],
      [615, 840],
      [450, 840]
    ])
    body(c, col, pts)
    shadeRight(c, 530, 615, 380, 840)
    collar(c, col, 208)
    reflective(c, 285, 615, 650, 26)
    c.line(690, 380, 730, 350, 4, col.dark, 0.5)
    c.line(210, 380, 170, 350, 4, col.dark, 0.5)
    c.rrect(500, 380, 90, 90, 8, col.accent, 0.85)
  },

  tank(c, col) {
    const pts = mirror([
      [450, 310],
      [500, 290],
      [515, 200],
      [585, 200],
      [598, 330],
      [640, 420],
      [625, 840],
      [450, 840]
    ])
    body(c, col, pts)
    shadeRight(c, 530, 630, 420, 840)
    reflective(c, 275, 625, 700, 24)
    c.line(300, 460, 600, 460, 3, col.dark, 0.35)
  },

  longsleeve(c, col) {
    const pts = mirror([
      [450, 205],
      [510, 205],
      [610, 232],
      [700, 262],
      [780, 725],
      [705, 742],
      [625, 430],
      [615, 440],
      [615, 840],
      [450, 840]
    ])
    body(c, col, pts)
    shadeRight(c, 540, 615, 440, 840)
    collar(c, col, 208)
    const cuff = [
      [782, 700],
      [705, 718],
      [700, 746],
      [778, 728]
    ]
    c.poly(cuff, col.dark)
    c.poly(cuff.map(mirrorPoint), col.dark)
    reflective(c, 285, 615, 620, 24)
    c.rrect(300, 380, 70, 70, 6, col.accent, 0.85)
  },

  hoodie(c, col) {
    c.ellipse(450, 225, 118, 92, col.dark)
    const pts = mirror([
      [450, 215],
      [520, 215],
      [615, 245],
      [705, 275],
      [785, 735],
      [705, 752],
      [630, 440],
      [622, 450],
      [622, 845],
      [450, 845]
    ])
    body(c, col, pts)
    shadeRight(c, 540, 622, 450, 845)
    c.ellipse(450, 245, 78, 58, darken(col.dark, 0.5))
    c.poly(
      [
        [340, 650],
        [560, 650],
        [610, 770],
        [290, 770]
      ],
      col.dark,
      0.55
    )
    c.line(420, 290, 418, 430, 6, col.light, 0.85)
    c.line(480, 290, 482, 430, 6, col.light, 0.85)
    c.rrect(290, 800, 320, 45, 8, col.dark)
    reflective(c, 300, 600, 560, 22)
  },

  jacket(c, col) {
    const pts = mirror([
      [450, 215],
      [520, 205],
      [620, 240],
      [710, 270],
      [790, 690],
      [715, 710],
      [640, 430],
      [628, 440],
      [628, 830],
      [450, 830]
    ])
    body(c, col, pts)
    shadeRight(c, 540, 628, 440, 830)
    c.poly(
      [
        [380, 185],
        [520, 185],
        [540, 245],
        [450, 275],
        [360, 245]
      ],
      col.dark
    )
    zipper(c, col, 270, 830)
    c.rrect(300, 620, 90, 130, 10, col.dark, 0.5)
    c.rrect(510, 620, 90, 130, 10, col.dark, 0.5)
    reflective(c, 272, 628, 440, 22)
    c.rrect(500, 330, 80, 50, 8, col.accent, 0.9)
  },

  parka(c, col) {
    c.ellipse(450, 225, 130, 100, col.dark)
    const pts = mirror([
      [450, 205],
      [520, 205],
      [620, 240],
      [710, 272],
      [795, 740],
      [720, 758],
      [650, 440],
      [636, 450],
      [646, 935],
      [450, 935]
    ])
    body(c, col, pts)
    shadeRight(c, 540, 646, 450, 935)
    c.ellipse(450, 250, 84, 62, darken(col.dark, 0.5))
    zipper(c, col, 300, 935)
    c.rrect(290, 660, 110, 150, 10, col.dark, 0.55)
    c.rrect(500, 660, 110, 150, 10, col.dark, 0.55)
    c.line(290, 560, 610, 560, 8, col.dark, 0.6)
    reflective(c, 262, 646, 870, 24)
    c.rrect(510, 340, 80, 50, 8, col.accent, 0.9)
  },

  vest(c, col) {
    const pts = mirror([
      [450, 215],
      [510, 205],
      [600, 232],
      [628, 335],
      [645, 430],
      [635, 830],
      [450, 830]
    ])
    body(c, col, pts)
    shadeRight(c, 540, 635, 430, 830)
    for (let y = 340; y < 830; y += 95) {
      c.line(265, y, 635, y, 4, col.dark, 0.4)
    }
    c.poly(
      [
        [385, 190],
        [515, 190],
        [535, 245],
        [450, 272],
        [365, 245]
      ],
      col.dark
    )
    zipper(c, col, 270, 830)
    reflective(c, 265, 635, 700, 22)
  },

  pants(c, col) {
    const pts = mirror([
      [450, 190],
      [630, 190],
      [645, 260],
      [655, 985],
      [500, 985],
      [465, 470],
      [450, 430]
    ])
    body(c, col, pts)
    shadeRight(c, 580, 655, 260, 985)
    c.rrect(270, 190, 360, 56, 8, col.dark)
    c.line(450, 246, 450, 432, 3, col.dark, 0.7)
    c.rrect(540, 520, 105, 130, 8, col.dark, 0.45)
    c.rrect(255, 520, 105, 130, 8, col.dark, 0.45)
    reflective(c, 500, 655, 905, 22)
    reflective(c, 245, 400, 905, 22)
    c.rrect(420, 200, 60, 30, 6, col.accent, 0.9)
  },

  jogger(c, col) {
    const pts = mirror([
      [450, 190],
      [630, 190],
      [655, 270],
      [640, 700],
      [585, 880],
      [592, 985],
      [500, 985],
      [498, 880],
      [470, 520],
      [450, 440]
    ])
    body(c, col, pts)
    shadeRight(c, 580, 650, 270, 985)
    c.rrect(270, 190, 360, 56, 8, col.dark)
    c.rrect(500, 940, 92, 45, 6, col.dark)
    c.rrect(308, 940, 92, 45, 6, col.dark)
    c.line(420, 250, 420, 350, 5, col.light, 0.8)
    c.line(480, 250, 480, 350, 5, col.light, 0.8)
    c.line(300, 560, 360, 800, 12, col.accent, 0.85)
    c.line(600, 560, 540, 800, 12, col.accent, 0.85)
  },

  shorts(c, col) {
    const pts = mirror([
      [450, 260],
      [625, 260],
      [645, 335],
      [655, 730],
      [485, 730],
      [465, 570],
      [450, 525]
    ])
    body(c, col, pts)
    shadeRight(c, 580, 655, 335, 730)
    c.rrect(275, 260, 350, 54, 8, col.dark)
    c.line(420, 318, 418, 420, 5, col.light, 0.85)
    c.line(480, 318, 482, 420, 5, col.light, 0.85)
    reflective(c, 485, 655, 660, 20)
    reflective(c, 245, 415, 660, 20)
    c.rrect(510, 440, 100, 110, 8, col.dark, 0.45)
  },

  sneaker(c, col) {
    c.ellipse(465, 770, 360, 28, [0, 0, 0], 0.35)
    const upper = [
      [150, 668],
      [148, 470],
      [205, 415],
      [310, 440],
      [375, 375],
      [445, 392],
      [520, 470],
      [640, 520],
      [770, 580],
      [795, 668]
    ]
    c.poly(upper, col.base)
    rim(c, upper)
    c.ellipse(705, 615, 95, 62, col.light, 0.18)
    c.poly(
      [
        [230, 640],
        [330, 600],
        [480, 525],
        [570, 500],
        [510, 565],
        [400, 625],
        [300, 665]
      ],
      col.accent,
      0.95
    )
    for (let i = 0; i < 4; i++) {
      c.line(395 + i * 36, 410 + i * 24, 425 + i * 36, 450 + i * 24, 9, [235, 238, 245], 0.9)
    }
    c.rrect(140, 668, 660, 72, 34, [236, 239, 246])
    c.line(150, 700, 790, 700, 6, col.accent, 0.9)
    c.rrect(140, 735, 650, 16, 8, [150, 154, 166])
    c.rrect(150, 520, 40, 120, 14, col.dark, 0.8)
  },

  boot(c, col) {
    c.ellipse(465, 790, 360, 28, [0, 0, 0], 0.35)
    const upper = [
      [160, 690],
      [150, 290],
      [310, 290],
      [335, 440],
      [425, 470],
      [640, 560],
      [780, 610],
      [800, 690]
    ]
    c.poly(upper, col.base)
    rim(c, upper)
    for (let i = 0; i < 5; i++) {
      c.line(200, 330 + i * 52, 300, 345 + i * 52, 9, [235, 238, 245], 0.9)
    }
    c.rrect(215, 285, 60, 60, 10, col.accent, 0.9)
    c.poly(
      [
        [155, 640],
        [330, 620],
        [480, 640],
        [640, 640],
        [790, 650],
        [800, 690],
        [160, 690]
      ],
      col.dark,
      0.5
    )
    c.rrect(145, 680, 665, 90, 22, [58, 58, 66])
    for (let x = 175; x < 790; x += 44) {
      c.rrect(x, 748, 26, 20, 5, [30, 30, 36])
    }
    c.line(160, 705, 795, 705, 5, col.accent, 0.9)
  },

  slide(c, col) {
    c.ellipse(465, 760, 340, 26, [0, 0, 0], 0.35)
    c.rrect(150, 660, 650, 90, 44, [236, 239, 246])
    c.rrect(150, 725, 650, 30, 15, [150, 154, 166])
    c.poly(
      [
        [290, 665],
        [340, 545],
        [560, 520],
        [690, 580],
        [720, 665]
      ],
      col.base
    )
    rim(c, [
      [290, 665],
      [340, 545],
      [560, 520],
      [690, 580],
      [720, 665]
    ])
    c.line(330, 600, 690, 605, 12, col.accent, 0.9)
  },

  boxer(c, col) {
    const pts = mirror([
      [450, 330],
      [620, 330],
      [640, 380],
      [695, 640],
      [520, 640],
      [470, 545],
      [450, 520]
    ])
    body(c, col, pts)
    shadeRight(c, 580, 695, 380, 640)
    c.rrect(260, 330, 380, 56, 8, col.accent, 0.92)
    for (let x = 290; x < 620; x += 40) {
      c.line(x, 345, x, 372, 5, [10, 10, 14], 0.55)
    }
    c.line(450, 386, 450, 520, 3, col.dark, 0.6)
  },

  socks(c, col) {
    const sock = [
      [400, 190],
      [560, 190],
      [562, 620],
      [700, 660],
      [775, 760],
      [745, 855],
      [380, 855],
      [372, 650],
      [400, 625]
    ]
    c.poly(sock, col.base)
    rim(c, sock)
    c.rrect(396, 190, 168, 60, 6, col.accent, 0.92)
    c.rrect(396, 262, 168, 14, 4, [236, 239, 246], 0.8)
    c.ellipse(400, 780, 52, 70, col.dark, 0.7)
    c.ellipse(730, 790, 58, 52, col.dark, 0.7)
    reflective(c, 398, 562, 420, 18)
  },

  cap(c, col) {
    const dome = [...arc(450, 600, 262, 250, 180, 360, 40)]
    c.poly(dome, col.base)
    rim(c, dome)
    c.poly(
      [...arc(450, 600, 262, 250, 270, 360, 20), [712, 600], [450, 600]],
      [0, 0, 0],
      0.14
    )
    for (const [x1, y1, x2, y2] of [
      [450, 350, 450, 600],
      [450, 350, 330, 600],
      [450, 350, 570, 600]
    ]) {
      c.line(x1, y1, x2, y2, 3, col.dark, 0.5)
    }
    c.ellipse(450, 352, 18, 18, col.dark)
    c.poly(
      [
        [190, 600],
        [710, 600],
        [840, 640],
        [790, 690],
        [450, 705],
        [210, 665]
      ],
      col.dark
    )
    c.rrect(190, 580, 520, 26, 12, col.accent, 0.95)
    c.rrect(380, 470, 140, 70, 10, col.accent, 0.85)
  },

  beanie(c, col) {
    const dome = [...arc(450, 640, 235, 330, 180, 360, 40)]
    c.poly(dome, col.base)
    rim(c, dome)
    for (let x = 260; x < 660; x += 40) {
      c.line(x, 360, x + (x < 450 ? -10 : 10), 640, 3, col.dark, 0.35)
    }
    c.rrect(205, 640, 490, 150, 22, col.dark)
    for (let x = 235; x < 680; x += 28) {
      c.line(x, 650, x, 780, 4, [0, 0, 0], 0.25)
    }
    c.rrect(380, 690, 140, 50, 10, col.accent, 0.92)
    c.ellipse(450, 320, 48, 36, col.light, 0.9)
  },

  backpack(c, col) {
    c.stroke(arc(450, 220, 70, 70, 180, 360, 16), 18, col.dark)
    c.rrect(240, 200, 420, 660, 80, col.base)
    rim(c, arc(450, 530, 210, 330, 0, 360, 48))
    c.rrect(280, 520, 340, 260, 40, col.dark, 0.7)
    c.line(300, 560, 600, 560, 4, col.light, 0.7)
    reflective(c, 290, 610, 640, 22)
    c.line(450, 240, 450, 500, 6, col.light, 0.7)
    c.rrect(405, 480, 90, 50, 10, col.accent, 0.95)
    c.poly(
      [
        [236, 300],
        [200, 380],
        [200, 740],
        [240, 760]
      ],
      col.dark
    )
    c.poly(
      [
        [664, 300],
        [700, 380],
        [700, 740],
        [660, 760]
      ],
      col.dark
    )
  },

  crossbody(c, col) {
    c.stroke(arc(450, 440, 190, 290, 180, 360, 30), 26, col.dark)
    c.rrect(250, 430, 400, 300, 44, col.base)
    c.poly(
      [
        [250, 475],
        [650, 475],
        [650, 520],
        [450, 585],
        [250, 520]
      ],
      col.dark,
      0.75
    )
    c.rrect(405, 570, 90, 54, 10, col.accent, 0.95)
    reflective(c, 262, 638, 660, 20)
    rim(c, arc(450, 580, 200, 150, 0, 360, 40))
  },

  shades(c, col) {
    c.ellipse(335, 560, 150, 105, col.base)
    c.ellipse(565, 560, 150, 105, col.base)
    c.ellipse(335, 560, 125, 82, [14, 14, 20])
    c.ellipse(565, 560, 125, 82, [14, 14, 20])
    c.ellipse(335, 560, 125, 82, col.accent, 0.35)
    c.ellipse(565, 560, 125, 82, col.accent, 0.35)
    c.line(300, 520, 370, 495, 10, [255, 255, 255], 0.35)
    c.line(530, 520, 600, 495, 10, [255, 255, 255], 0.35)
    c.rrect(405, 535, 90, 24, 12, col.base)
    c.line(185, 545, 90, 505, 22, col.base)
    c.line(715, 545, 810, 505, 22, col.base)
  },

  bottle(c, col) {
    c.rrect(360, 300, 180, 640, 52, col.base)
    rim(c, arc(450, 620, 90, 320, 0, 360, 40))
    c.rrect(390, 190, 120, 120, 22, col.dark)
    c.stroke(arc(450, 200, 40, 40, 180, 360, 12), 14, col.dark)
    c.rrect(360, 440, 180, 120, 0, col.accent, 0.92)
    reflective(c, 360, 540, 700, 22)
    c.rrect(372, 320, 18, 560, 9, [255, 255, 255], 0.14)
  },

  wallet(c, col) {
    c.rrect(280, 390, 350, 60, 10, lighten(col.dark, 0.3))
    c.rrect(265, 410, 380, 60, 10, col.accent, 0.9)
    c.rrect(250, 440, 400, 290, 40, col.base)
    rim(c, arc(450, 585, 200, 145, 0, 360, 40))
    for (let x = 280; x < 620; x += 26) {
      c.line(x, 468, x + 14, 468, 4, col.light, 0.55)
      c.line(x, 702, x + 14, 702, 4, col.light, 0.55)
    }
    c.ellipse(450, 590, 26, 26, col.accent)
  },

  scarf(c, col) {
    const pts = [
      [260, 300],
      [640, 300],
      [662, 830],
      [238, 830]
    ]
    c.poly(pts, col.base)
    rim(c, pts)
    c.ellipse(450, 300, 190, 36, col.dark)
    for (let y = 400; y < 830; y += 120) {
      c.poly(
        [
          [253, y],
          [647, y],
          [650, y + 26],
          [250, y + 26]
        ],
        col.accent,
        y === 520 ? 0.95 : 0.55
      )
    }
    reflective(c, 246, 654, 700, 24)
  },

  belt(c, col) {
    c.rrect(110, 540, 680, 64, 14, col.base)
    for (let x = 160; x < 640; x += 36) {
      c.ellipse(x, 572, 7, 7, [10, 10, 14], 0.8)
    }
    c.line(110, 556, 790, 556, 3, col.light, 0.4)
    c.line(110, 590, 790, 590, 3, col.dark, 0.5)
    c.rrect(640, 505, 130, 134, 14, [210, 214, 224])
    c.rrect(662, 527, 86, 90, 8, [20, 20, 28])
    c.rrect(645, 555, 100, 14, 4, [210, 214, 224])
    c.rrect(160, 540, 40, 64, 4, col.accent, 0.9)
  }
}

function accentFor(color) {
  const index = [...color].reduce((sum, char) => sum + char.charCodeAt(0), 0) % ACCENTS.length
  const picked = ACCENTS[index]
  return picked === PALETTE[color] ? ACCENTS[(index + 1) % ACCENTS.length] : picked
}

function crc32(buffer) {
  let crc = 0xffffffff
  for (const byte of buffer) {
    crc ^= byte
    for (let k = 0; k < 8; k++) {
      crc = crc & 1 ? (crc >>> 1) ^ 0xedb88320 : crc >>> 1
    }
  }
  return (crc ^ 0xffffffff) >>> 0
}

function chunk(type, data) {
  const length = Buffer.alloc(4)
  length.writeUInt32BE(data.length)
  const typeAndData = Buffer.concat([Buffer.from(type, 'ascii'), data])
  const crc = Buffer.alloc(4)
  crc.writeUInt32BE(crc32(typeAndData))
  return Buffer.concat([length, typeAndData, crc])
}

function encodePng(rgb) {
  const header = Buffer.alloc(13)
  header.writeUInt32BE(WIDTH, 0)
  header.writeUInt32BE(HEIGHT, 4)
  header[8] = 8
  header[9] = 2
  const raw = Buffer.alloc((WIDTH * 3 + 1) * HEIGHT)
  for (let y = 0; y < HEIGHT; y++) {
    rgb.copy(raw, y * (WIDTH * 3 + 1) + 1, y * WIDTH * 3, (y + 1) * WIDTH * 3)
  }
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(raw, { level: 6 })),
    chunk('IEND', Buffer.alloc(0))
  ])
}

const OFFSET_Y = {
  tee: 40,
  tank: 40,
  longsleeve: 40,
  hoodie: 35,
  jacket: 40,
  vest: 40,
  shorts: 65,
  boot: 30,
  slide: -75,
  boxer: 80,
  socks: 40,
  cap: 35,
  beanie: 10,
  backpack: 65,
  crossbody: 120,
  scarf: 15
}

export const SHAPES = Object.keys(DRAW)

export function renderProductImage(shape, colorName) {
  const draw = DRAW[shape]
  if (!draw) {
    throw new Error(`Unknown shape: ${shape}`)
  }
  const base = hex(PALETTE[colorName])
  const accent = hex(accentFor(colorName))
  const col = {
    base,
    dark: darken(base, 0.35),
    light: lighten(base, 0.45),
    accent
  }
  const canvas = new Canvas()
  canvas.background(accent)
  canvas.ty = OFFSET_Y[shape] ?? 0
  draw(canvas, col)
  return encodePng(canvas.downsample())
}

export const IMAGE_COLORS = Object.keys(PALETTE)
