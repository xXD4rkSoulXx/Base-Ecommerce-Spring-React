const APPAREL_SIZES = ['S', 'M', 'L', 'XL']
const BASIC_SIZES = ['S', 'M', 'L']
const SHOE_SIZES = ['_40', '_42', '_44']
const ONE_SIZE = ['ONE_SIZE']

// shape: silhouette used by the image generator
// weight (g) and dims (cm) are the packed size of one item, they must fit the configured shipping boxes
export const TYPES = {
  jacket: {
    shape: 'jacket',
    category: 'OUTERWEAR',
    code: 'OUT',
    sizes: APPAREL_SIZES,
    weight: 900,
    dims: [38, 30, 8],
    blurb:
      'Water-repellent ripstop shell with taped seams, a storm collar and two zip pockets. Reflective chest and hem details keep you visible in traffic after dark. Regular fit.'
  },
  parka: {
    shape: 'parka',
    category: 'OUTERWEAR',
    code: 'OUT',
    sizes: APPAREL_SIZES,
    weight: 1300,
    dims: [42, 32, 12],
    blurb:
      'Longer cut with an adjustable hood, a two-way front zip and deep hand-warmer pockets. Concrete-grade textile outer, soft brushed lining and a reflective hem band. Relaxed fit.'
  },
  vest: {
    shape: 'vest',
    category: 'OUTERWEAR',
    code: 'OUT',
    sizes: APPAREL_SIZES,
    weight: 600,
    dims: [36, 28, 8],
    blurb:
      'Lightweight insulated vest with channel quilting and a full-length zip. Layers over anything without bulk, and a reflective band keeps the back visible. Regular fit.'
  },
  tee: {
    shape: 'tee',
    category: 'TOPS',
    code: 'TOP',
    sizes: APPAREL_SIZES,
    weight: 220,
    dims: [30, 24, 3],
    blurb:
      'Heavyweight organic cotton jersey with a ribbed collar and a reflective hem tape. Pre-washed for a soft hand feel and built to keep its shape. Relaxed fit.'
  },
  longsleeve: {
    shape: 'longsleeve',
    category: 'TOPS',
    code: 'TOP',
    sizes: APPAREL_SIZES,
    weight: 320,
    dims: [32, 24, 3],
    blurb:
      'Mid-weight cotton blend with ribbed cuffs and a clean crew neck. A reflective hem tape and a small woven label keep the details sharp. Regular fit.'
  },
  hoodie: {
    shape: 'hoodie',
    category: 'TOPS',
    code: 'TOP',
    sizes: APPAREL_SIZES,
    weight: 650,
    dims: [36, 28, 7],
    blurb:
      'Brushed-back fleece with a double-lined hood, a kangaroo pocket and flat drawcords with metal tips. Ribbed cuffs and hem, and a reflective chest panel. Relaxed fit.'
  },
  tank: {
    shape: 'tank',
    category: 'TOPS',
    code: 'TOP',
    sizes: APPAREL_SIZES,
    weight: 150,
    dims: [28, 22, 2],
    blurb:
      'Breathable stretch jersey with a deep scoop neck and bound armholes. Light enough for the hottest ride, with a reflective hem tape. Regular fit.'
  },
  pants: {
    shape: 'pants',
    category: 'BOTTOMS',
    code: 'BOT',
    sizes: BASIC_SIZES,
    weight: 650,
    dims: [36, 26, 5],
    blurb:
      'Durable stretch twill with articulated knees, a gusseted crotch and reinforced pockets. Reflective ankle tabs fold out of the way when you roll the hem. Straight fit.'
  },
  jogger: {
    shape: 'jogger',
    category: 'BOTTOMS',
    code: 'BOT',
    sizes: BASIC_SIZES,
    weight: 600,
    dims: [35, 26, 5],
    blurb:
      'Soft brushed fleece with an elasticated waist, flat drawcords and ribbed ankle cuffs. Side zip pockets keep your keys where they belong. Tapered fit.'
  },
  shorts: {
    shape: 'shorts',
    category: 'BOTTOMS',
    code: 'BOT',
    sizes: BASIC_SIZES,
    weight: 350,
    dims: [30, 24, 4],
    blurb:
      'Quick-dry woven fabric with an elasticated waist, a flat drawcord and zip pockets. Reflective leg tabs for early and late sessions. Knee-length cut.'
  },
  sneaker: {
    shape: 'sneaker',
    category: 'FOOTWEAR',
    code: 'FTW',
    sizes: SHOE_SIZES,
    weight: 950,
    dims: [34, 23, 13],
    blurb:
      'Lightweight engineered upper on a cushioned midsole with a grippy rubber outsole. Padded collar, reflective heel tab and a neon accent stripe. True to size.'
  },
  boot: {
    shape: 'boot',
    category: 'FOOTWEAR',
    code: 'FTW',
    sizes: SHOE_SIZES,
    weight: 1400,
    dims: [34, 26, 18],
    blurb:
      'Water-resistant upper with a padded ankle, a chunky lug outsole and a pull tab. Built for wet pavement and long walks across town. True to size.'
  },
  slide: {
    shape: 'slide',
    category: 'FOOTWEAR',
    code: 'FTW',
    sizes: SHOE_SIZES,
    weight: 450,
    dims: [32, 12, 9],
    blurb:
      'Cushioned footbed with a moulded arch, a wide padded strap and a textured outsole. Easy to slip on after training. True to size.'
  },
  boxer: {
    shape: 'boxer',
    category: 'UNDERWEAR',
    code: 'UND',
    sizes: BASIC_SIZES,
    weight: 120,
    dims: [22, 16, 3],
    blurb: 'Stretch cotton with a soft elastic waistband, a flat-lock seam and a contoured pouch. No tags, no itch. Fitted.'
  },
  socks: {
    shape: 'socks',
    category: 'UNDERWEAR',
    code: 'UND',
    sizes: BASIC_SIZES,
    weight: 90,
    dims: [20, 12, 3],
    blurb:
      'Cushioned terry sole with a ribbed arch band and a reinforced heel and toe. A reflective yarn stripe on the cuff catches the light. Mid-calf length.'
  },
  undershirt: {
    shape: 'tank',
    category: 'UNDERWEAR',
    code: 'UND',
    sizes: BASIC_SIZES,
    weight: 140,
    dims: [28, 22, 2],
    blurb:
      'Fine stretch jersey that sits flat under any layer, with bound edges and no side seams. Wicks moisture and dries fast. Fitted.'
  },
  cap: {
    shape: 'cap',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 110,
    dims: [26, 22, 12],
    blurb:
      'Six-panel cotton twill with a pre-curved brim, a reflective band and an adjustable strap with a metal buckle. One size fits most.'
  },
  beanie: {
    shape: 'beanie',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 90,
    dims: [22, 18, 6],
    blurb: 'Soft rib-knit acrylic and wool blend with a deep cuff and a small woven label. Warm without the itch. One size fits most.'
  },
  backpack: {
    shape: 'backpack',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 850,
    dims: [40, 30, 14],
    blurb:
      'Water-repellent coated fabric with a padded laptop sleeve, a quick-access front pocket and reflective panels. Padded straps and a sternum clip. One size.'
  },
  crossbody: {
    shape: 'crossbody',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 400,
    dims: [28, 20, 9],
    blurb:
      'Compact bag with a magnetic flap, an internal zip pocket and an adjustable strap with a quick-release buckle. Carries a phone, keys, wallet and a small lock. One size.'
  },
  shades: {
    shape: 'shades',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 60,
    dims: [16, 6, 5],
    blurb: 'Lightweight frame with polarised UV400 lenses, flexible temples and non-slip nose pads. Comes with a soft pouch. One size.'
  },
  bottle: {
    shape: 'bottle',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 350,
    dims: [27, 8, 8],
    blurb:
      'Double-wall stainless steel keeps drinks cold for 24 hours or hot for 12. Leak-proof lid with a carry loop and a powder-coated finish.'
  },
  wallet: {
    shape: 'wallet',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 70,
    dims: [12, 10, 2],
    blurb: 'Slim vegan-leather wallet with card slots, a cash pocket and contrast stitching. Fits the front pocket without a bulge. One size.'
  },
  scarf: {
    shape: 'scarf',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 70,
    dims: [24, 18, 2],
    blurb:
      'Seamless stretch neck gaiter that can be worn as a collar, a face cover or a headband. Reflective bands on the front. One size.'
  },
  belt: {
    shape: 'belt',
    category: 'ACCESSORIES',
    code: 'ACC',
    sizes: ONE_SIZE,
    weight: 150,
    dims: [30, 12, 4],
    blurb:
      'Woven webbing belt with a metal slider buckle and no holes, so the fit is fully adjustable. Strong enough for a tool loop. One size.'
  }
}

// [name, type, price, discount %, colors, tagline]
const ROWS = [
  ['Static Shell Jacket', 'jacket', 189.9, 0, 'BLACK,CYAN', 'Three-layer shell built for sideways rain and last-train sprints.'],
  ['Night Rider Windbreaker', 'jacket', 99.9, 15, 'LIME,BLACK', 'Packs into its own pocket and glows when the headlights hit.'],
  ['Concrete Parka', 'parka', 229, 0, 'GRAY,OLIVE', 'The long one. Heavy-duty protection for the coldest commute.'],
  ['Reflex Bomber', 'jacket', 139.9, 0, 'BLACK', 'A classic bomber silhouette with reflective piping.'],
  ['Overcast Coach Jacket', 'jacket', 109.9, 20, 'GRAY,WHITE', 'Snap-front coach jacket for grey days and neon nights.'],
  ['Neon Drift Vest', 'vest', 79.9, 0, 'MAGENTA,BLACK', 'Visibility you can wear to dinner.'],
  ['Grid Puffer Vest', 'vest', 99.9, 25, 'OLIVE', 'Core warmth without the sleeves.'],
  ['Transit Trench', 'parka', 249, 0, 'BLACK', 'A waterproof trench for people who never stop moving.'],
  ['Voltage Track Jacket', 'jacket', 89.9, 0, 'BLUE,WHITE', 'Retro track jacket, rebuilt with recycled fabric.'],
  ['Asphalt Fleece Zip', 'jacket', 84.9, 10, 'GRAY,BLACK', 'Mid-layer fleece that works under a shell or on its own.'],
  ['Downtown Anorak', 'parka', 149.9, 0, 'ORANGE,BLACK', 'Pull-over anorak with a big kangaroo pocket.'],
  ['Halo Rain Shell', 'jacket', 129.9, 30, 'YELLOW', 'Feather-light rain shell in unmissable yellow.'],
  ['Signal Quilted Liner', 'vest', 89.9, 0, 'TEAL,BLACK', 'A liner vest that lives under everything.'],
  ['Midnight Harrington', 'jacket', 119.9, 0, 'BLACK,MAROON', 'Clean lines and a snap collar for after-hours.'],

  ['Raw Urban Tee', 'tee', 34.9, 0, 'BLACK,WHITE', 'The everyday tee, no logos, no noise.'],
  ['Lumen Pocket Tee', 'tee', 39.9, 0, 'WHITE', 'A chest pocket in a contrast neon, nothing more.'],
  ['Static Boxy Tee', 'tee', 36.9, 15, 'GRAY,BLACK', 'Wide, short and heavy. Boxy on purpose.'],
  ['Afterglow Tee', 'tee', 32.9, 0, 'MAGENTA', 'Saturated colour that holds up after fifty washes.'],
  ['Signal Graphic Tee', 'tee', 39.9, 0, 'CYAN,BLACK', 'A bold tape detail across the hem.'],
  ['Concrete Heavy Tee', 'tee', 44.9, 0, 'GRAY', 'A 280 gsm jersey with serious weight.'],
  ['Mono Grid Tee', 'tee', 29.9, 20, 'WHITE,BLACK', 'Back to basics in two colours.'],
  ['Pulse Oversized Tee', 'tee', 42.9, 0, 'LIME', 'Oversized and drop-shouldered in an electric lime.'],
  ['Dispatch Tee', 'tee', 34.9, 0, 'OLIVE,WHITE', 'A courier-inspired tee with a utility feel.'],
  ['Night Shift Long Sleeve', 'longsleeve', 49.9, 0, 'BLACK', 'A black long sleeve for every shift.'],
  ['Reflect Thermal Long Sleeve', 'longsleeve', 54.9, 10, 'GRAY,CYAN', 'Waffle-knit warmth with reflective cuffs.'],
  ['Underpass Rib Long Sleeve', 'longsleeve', 44.9, 0, 'MAROON', 'Rib knit, slim and a little vintage.'],
  ['Courier Long Sleeve', 'longsleeve', 46.9, 0, 'WHITE,BLUE', 'A clean long sleeve with contrast cuffs.'],
  ['Frequency Crew Sweat', 'longsleeve', 64.9, 25, 'BLACK,PURPLE', 'Heavy crewneck sweat with a brushed interior.'],
  ['Deep Neon Hoodie', 'hoodie', 89.9, 0, 'MAGENTA,BLACK', 'Our loudest hoodie, by far.'],
  ['Static Heavy Hoodie', 'hoodie', 94.9, 0, 'GRAY,BLACK', 'The heavyweight. Built to last for years.'],
  ['Overpass Zip Hoodie', 'hoodie', 99.9, 15, 'BLACK', 'A full zip with a double-layer hood.'],
  ['Asphalt Half-Zip', 'hoodie', 84.9, 0, 'OLIVE', 'A half-zip pullover with a stand collar.'],
  ['Ghost Pullover Hoodie', 'hoodie', 79.9, 0, 'WHITE,GRAY', 'Tonal, quiet and impossibly soft.'],
  ['Voltage Fleece Hoodie', 'hoodie', 89.9, 20, 'BLUE,BLACK', 'A colour-blocked fleece hoodie.'],
  ['Heat Mesh Tank', 'tank', 27.9, 0, 'ORANGE', 'Ventilated mesh for hot days.'],
  ['Rooftop Tank', 'tank', 24.9, 0, 'WHITE,BLACK', 'A summer staple with a deep armhole.'],
  ['Sprint Singlet', 'tank', 29.9, 10, 'CYAN', 'A racing singlet with a reflective hem.'],
  ['Static Rib Tank', 'tank', 26.9, 0, 'BLACK,PINK', 'A ribbed tank with a fitted shape.'],

  ['Utility Cargo Pant', 'pants', 89.9, 0, 'OLIVE,BLACK', 'Six pockets, zero compromise.'],
  ['Slate Tech Trouser', 'pants', 94.9, 0, 'GRAY', 'A tailored trouser in a stretch tech fabric.'],
  ['Grid Cargo Pant', 'pants', 79.9, 20, 'BLACK', 'Cargo pockets on a slim cut.'],
  ['Reflex Straight Pant', 'pants', 84.9, 0, 'BLUE,BLACK', 'A straight leg with reflective tabs at the ankle.'],
  ['Concrete Work Pant', 'pants', 74.9, 0, 'GOLD', 'Workwear styling in a hard-wearing canvas.'],
  ['Night Run Pant', 'pants', 69.9, 15, 'BLACK', 'Light, stretchy and stealthy.'],
  ['Tapered Transit Pant', 'pants', 89.9, 0, 'WHITE,BLACK', 'A tapered trouser that goes from bike to bar.'],
  ['Static Fleece Jogger', 'jogger', 69.9, 0, 'GRAY,BLACK', 'Pure comfort in a heavyweight fleece.'],
  ['Pulse Track Jogger', 'jogger', 64.9, 10, 'MAGENTA,BLACK', 'A track jogger with a bold side stripe.'],
  ['Overcast Jogger', 'jogger', 59.9, 0, 'GRAY', 'The one you will live in all winter.'],
  ['Courier Cuff Jogger', 'jogger', 67.9, 0, 'BLACK,CYAN', 'Rib-cuff jogger with a zip pocket.'],
  ['Midnight Sweatpant', 'jogger', 59.9, 25, 'BLACK', 'A straight sweatpant, in black.'],
  ['Sprint Short', 'shorts', 39.9, 0, 'LIME,BLACK', 'A lightweight run short.'],
  ['Cargo Utility Short', 'shorts', 49.9, 0, 'OLIVE', 'Cargo pockets, knee length.'],
  ['Voltage Mesh Short', 'shorts', 36.9, 20, 'CYAN,BLACK', 'A basketball-inspired mesh short.'],
  ['Concrete Bermuda', 'shorts', 52.9, 0, 'GRAY', 'A tailored bermuda in a tech twill.'],
  ['Reflex Run Short', 'shorts', 41.9, 0, 'ORANGE,BLACK', 'Run short with a built-in key pocket.'],
  ['Static Fleece Short', 'shorts', 44.9, 15, 'BLACK,WHITE', 'The fleece short for warm evenings.'],

  ['Velo Runner 01', 'sneaker', 129.9, 0, 'WHITE,BLACK', 'Our signature runner, designed for the city.'],
  ['Static Low', 'sneaker', 99.9, 0, 'WHITE,GRAY', 'A clean low-top for everyday wear.'],
  ['Neon Drift High', 'sneaker', 119.9, 15, 'BLACK,MAGENTA', 'A high-top with an unmistakable accent stripe.'],
  ['Courier Trainer', 'sneaker', 109.9, 0, 'GRAY,LIME', 'A trainer built for long days on your feet.'],
  ['Asphalt Skate', 'sneaker', 89.9, 0, 'BLACK', 'A reinforced skate shoe with a vulcanised sole.'],
  ['Pulse Racer', 'sneaker', 139.9, 20, 'ORANGE,WHITE', 'A fast, lightweight racer.'],
  ['Overpass Mid', 'sneaker', 124.9, 0, 'BLUE,WHITE', 'A mid-top with ankle support.'],
  ['Ghost Knit Runner', 'sneaker', 134.9, 0, 'WHITE', 'A seamless knit runner, light as a sock.'],
  ['Signal Court', 'sneaker', 94.9, 25, 'YELLOW,BLACK', 'A court-inspired sneaker in a loud colourway.'],
  ['Concrete Boot', 'boot', 159.9, 0, 'BLACK,OLIVE', 'A tough lug-sole boot for bad weather.'],
  ['Night Patrol Boot', 'boot', 169.9, 0, 'BLACK', 'An all-black boot that goes with everything.'],
  ['Transit Chelsea', 'boot', 149.9, 10, 'MAROON,BLACK', 'A Chelsea boot with a chunky sole.'],
  ['Utility Lug Boot', 'boot', 179.9, 0, 'OLIVE', 'A work boot in a weatherproof canvas.'],
  ['Static Slide', 'slide', 39.9, 0, 'BLACK,WHITE', 'A slide for the post-workout shuffle.'],
  ['Pool Pulse Slide', 'slide', 34.9, 20, 'CYAN,PINK', 'A bright slide for hot days.'],
  ['Reflex Recovery Slide', 'slide', 44.9, 0, 'GRAY,LIME', 'An ultra-soft recovery slide.'],

  ['Static Boxer Brief', 'boxer', 22.9, 0, 'BLACK,GRAY', 'The everyday boxer brief.'],
  ['Mono Trunk', 'boxer', 19.9, 0, 'WHITE,BLACK', 'A short trunk in soft cotton.'],
  ['Mesh Sport Brief', 'boxer', 24.9, 10, 'BLUE', 'A breathable brief for training.'],
  ['Night Boxer', 'boxer', 21.9, 0, 'BLACK,MAROON', 'A dark boxer with a contrast waistband.'],
  ['Reflex Crew Sock', 'socks', 12.9, 0, 'WHITE,BLACK', 'A cushioned crew sock.'],
  ['Pulse Ankle Sock', 'socks', 10.9, 0, 'LIME', 'A bright ankle sock for sneakers.'],
  ['Grid Tube Sock', 'socks', 11.9, 25, 'GRAY,WHITE', 'A classic tube sock with a ribbed cuff.'],
  ['Thermal Wool Sock', 'socks', 15.9, 0, 'OLIVE,BLACK', 'A warm wool sock for winter boots.'],
  ['Base Layer Tank', 'undershirt', 24.9, 0, 'BLACK,WHITE', 'A smooth base layer tank.'],
  ['Rib Undershirt', 'undershirt', 19.9, 15, 'WHITE', 'A ribbed undershirt with a slim fit.'],

  ['Static 6-Panel Cap', 'cap', 29.9, 0, 'BLACK,CYAN', 'A minimal six-panel cap.'],
  ['Reflex Dad Cap', 'cap', 27.9, 0, 'WHITE', 'A relaxed dad cap with a curved brim.'],
  ['Night Rider Cap', 'cap', 29.9, 15, 'BLACK', 'A low-profile cap with a reflective band.'],
  ['Mesh Runner Cap', 'cap', 24.9, 0, 'LIME,BLACK', 'A lightweight mesh-back cap.'],
  ['Rib Watch Beanie', 'beanie', 22.9, 0, 'BLACK,GRAY', 'The classic watch cap.'],
  ['Neon Cuff Beanie', 'beanie', 24.9, 20, 'MAGENTA,BLACK', 'A beanie with a neon cuff.'],
  ['Static Knit Beanie', 'beanie', 19.9, 0, 'OLIVE', 'A chunky knit beanie.'],
  ['Courier Backpack 22L', 'backpack', 89.9, 0, 'BLACK,CYAN', 'A 22-litre pack for the daily ride.'],
  ['Rooftop Pack 30L', 'backpack', 109.9, 10, 'GRAY,ORANGE', 'A 30-litre pack for weekends away.'],
  ['Dispatch Crossbody', 'crossbody', 59.9, 0, 'BLACK,LIME', 'A crossbody bag for the essentials.'],
  ['Micro Sling Bag', 'crossbody', 44.9, 0, 'WHITE', 'A slim sling for phone and keys.'],
  ['Signal Shades', 'shades', 49.9, 0, 'MAGENTA,BLACK', 'Round shades with polarised lenses.'],
  ['Night Mirror Shades', 'shades', 54.9, 25, 'BLACK', 'Mirrored lenses with a slim frame.'],
  ['Insulated Bottle 600ml', 'bottle', 26.9, 0, 'LIME,BLACK', 'A bottle that keeps its temperature all day.'],
  ['Utility Card Wallet', 'wallet', 29.9, 0, 'BLACK,ORANGE', 'A slim card wallet.'],
  ['Zip Pocket Wallet', 'wallet', 34.9, 0, 'GRAY', 'A wallet with a zipped coin pocket.'],
  ['Reflective Neck Gaiter', 'scarf', 21.9, 0, 'BLACK,LIME', 'A gaiter with reflective stripes.'],
  ['Webbing Belt', 'belt', 27.9, 10, 'BLACK,OLIVE', 'A tough webbing belt with a metal slider.']
]

const FEATURED = {
  'Static Shell Jacket': 'LEFT',
  'Velo Runner 01': 'RIGHT'
}

const counters = {}

export const PRODUCTS = ROWS.map(([name, type, price, discount, colors, tagline]) => {
  const definition = TYPES[type]
  counters[definition.code] = (counters[definition.code] ?? 0) + 1
  return {
    ref: `VC-${definition.code}-${String(counters[definition.code]).padStart(3, '0')}`,
    name,
    type,
    shape: definition.shape,
    category: definition.category,
    price,
    discountPercentage: discount,
    colors: colors.split(','),
    sizes: definition.sizes,
    weightGrams: definition.weight,
    lengthCm: definition.dims[0],
    widthCm: definition.dims[1],
    heightCm: definition.dims[2],
    description: `${tagline} ${definition.blurb}`,
    featuredSlot: FEATURED[name] ?? null
  }
})
