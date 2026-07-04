package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val database: AppDatabase) {
    private val profileDao = database.userProfileDao()
    private val textDao = database.textItemDao()
    private val savedWordDao = database.savedWordDao()
    private val translationCacheDao = database.translationCacheDao()

    val userProfileFlow: Flow<UserProfile?> = profileDao.getUserProfileFlow()
    val allTextsFlow: Flow<List<TextItem>> = textDao.getAllTextsFlow()
    val allSavedWordsFlow: Flow<List<SavedWord>> = savedWordDao.getAllSavedWordsFlow()

    suspend fun getCache(cacheKey: String): TranslationCache? {
        return translationCacheDao.getCache(cacheKey)
    }

    suspend fun saveCache(cache: TranslationCache) {
        translationCacheDao.insertCache(cache)
    }

    suspend fun clearAllCache() {
        translationCacheDao.clearAllCache()
    }

    suspend fun getProfile(): UserProfile? {
        return profileDao.getUserProfile()
    }

    suspend fun saveProfile(profile: UserProfile) {
        profileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.updateProfile(profile)
    }

    suspend fun insertText(text: TextItem) {
        textDao.insertText(text)
    }

    suspend fun deleteTextById(id: Int) {
        textDao.deleteTextById(id)
    }

    suspend fun saveWord(word: SavedWord) {
        savedWordDao.insertWord(word)
    }

    suspend fun deleteWord(word: String) {
        savedWordDao.deleteWord(word)
    }

    suspend fun getSavedWord(word: String): SavedWord? {
        return savedWordDao.getSavedWord(word)
    }

    suspend fun populateDefaultTextsIfNeeded() {
        val currentTexts = textDao.getAllTextsFlow().firstOrNull() ?: emptyList()
        if (currentTexts.isEmpty()) {
            val defaultTexts = listOf(
                // 1. Alemão
                TextItem(
                    title = "Der Kölner Dom",
                    translationTitle = "A Catedral de Colônia",
                    content = "Der Kölner Dom ist eine berühmte Kirche in Deutschland. Er ist sehr hoch und wunderschön. Viele Menschen besuchen diesen Ort jedes Jahr.",
                    translationContent = "A Catedral de Colônia é uma igreja famosa na Alemanha. É muito alta e bonita. Muitas pessoas visitam este lugar todos os anos.",
                    level = "N4",
                    language = "Alemão",
                    category = "Cultura",
                    tags = "Cultura, História, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1549693578-d683be217e58?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "Das Oktoberfest in München",
                    translationTitle = "A Oktoberfest em Munique",
                    content = "Das Oktoberfest ist das größte Volksfest der Welt. Es findet in München statt. Menschen trinken Bier und essen traditionelle Brezeln.",
                    translationContent = "A Oktoberfest é o maior festival folclórico do mundo. Acontece em Munique. As pessoas bebem cerveja e comem pretzels tradicionais.",
                    level = "N3",
                    language = "Alemão",
                    category = "Estilo de Vida",
                    tags = "Cultura, Culinária, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?q=80&w=600&auto=format&fit=crop"
                ),
                // 2. Espanhol
                TextItem(
                    title = "La Sagrada Familia",
                    translationTitle = "A Sagrada Família",
                    content = "La Sagrada Familia es una gran iglesia en Barcelona. Fue diseñada por Antoni Gaudí. Es un símbolo de la hermosa arquitectura catalana.",
                    translationContent = "A Sagrada Família é uma grande igreja em Barcelona. Foi desenhada por Antoni Gaudí. É um símbolo da bela arquitetura catalã.",
                    level = "N4",
                    language = "Espanhol",
                    category = "Arquitetura",
                    tags = "Cultura, Viagem, Arquitetura",
                    imageUrl = "https://images.unsplash.com/photo-1583778176476-4a8b02a64c01?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "El festival de la Tomatina",
                    translationTitle = "O festival da Tomatina",
                    content = "La Tomatina es una fiesta muy divertida en Buñol. La gente se tira tomates en las calles. Ocurre el último miércoles de agosto.",
                    translationContent = "A Tomatina é uma festa muito divertida em Buñol. As pessoas atiram tomates umas nas outras nas ruas. Ocorre na última quarta-feira de agosto.",
                    level = "N3",
                    language = "Espanhol",
                    category = "Entretenimento",
                    tags = "Entretenimento, Cultura, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?q=80&w=600&auto=format&fit=crop"
                ),
                // 3. Grego
                TextItem(
                    title = "Ο Παρθενώνας",
                    translationTitle = "O Partenon",
                    content = "Ο Παρθενώνας είναι ένας αρχαίος ναός στην Αθήνα. Είναι αφιερωμένος στη θεά Αθηνά. Είναι σύμβολο της αρχαίας ελληνικής ιστορίας.",
                    translationContent = "O Partenon é um templo antigo em Atenas. É dedicado à deusa Atena. É um símbolo da antiga história grega.",
                    level = "N4",
                    language = "Grego",
                    category = "Cultura",
                    tags = "História, Cultura, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1503152394-c571994fd383?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "Το ελληνικό φαγητό",
                    translationTitle = "A comida grega",
                    content = "Το ελληνικό φαγητό είναι πολύ νόστιμο και υγιεινό. Περιλαμβάνει ελαιόλαδο, φέτα και φρέσκα λαχανικά. Η χωριάτικη σαλάτα είναι διάσημη.",
                    translationContent = "A comida grega é muito saborosa e saudável. Inclui azeite de oliva, queijo feta e vegetais frescos. A salada grega é famosa.",
                    level = "N3",
                    language = "Grego",
                    category = "Estilo de Vida",
                    tags = "Culinária, Estilo de Vida, Cultura",
                    imageUrl = "https://images.unsplash.com/photo-1532635241-17e820acf59f?q=80&w=600&auto=format&fit=crop"
                ),
                // 4. Hebraico
                TextItem(
                    title = "ירושלים העתיקה",
                    translationTitle = "A Jerusalém Antiga",
                    content = "ירושלים היא עיר עתיקה ויפהפייה. יש בה היסטוריה ארוכה וחשובה מאוד. אנשים רבים מבקרים בכותל המערבי בכל שנה.",
                    translationContent = "Jerusalém é uma cidade antiga e bonita. Tem uma história longa e muito importante. Muitas pessoas visitam o Muro das Lamentações a cada ano.",
                    level = "N4",
                    language = "Hebraico",
                    category = "Cultura",
                    tags = "História, Cultura, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1542856391-010fb87dcfed?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "הים התיכון",
                    translationTitle = "O Mar Mediterrâneo",
                    content = "הים התיכון מציע חופים מדהימים ומזג אוויר חם. תל אביב שוכנת על שפת הים ויש בה אוכל רחוב טעים כמו פלאפל.",
                    translationContent = "O Mar Mediterrâneo oferece praias incríveis e clima quente. Tel Aviv fica na costa e tem uma deliciosa comida de rua, como o falafel.",
                    level = "N3",
                    language = "Hebraico",
                    category = "Estilo de Vida",
                    tags = "Viagem, Estilo de Vida, Culinária",
                    imageUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?q=80&w=600&auto=format&fit=crop"
                ),
                // 5. Indonésio
                TextItem(
                    title = "Candi Borobudur",
                    translationTitle = "O Templo de Borobudur",
                    content = "Candi Borobudur adalah kuil Buddha terbesar di dunia. Terletak di Jawa Tengah, Indonesia. Tempat ini sangat indah dan bersejarah.",
                    translationContent = "O Templo de Borobudur é o maior templo budista do mundo. Fica em Java Central, Indonésia. Este lugar é muito bonito e histórico.",
                    level = "N4",
                    language = "Indonésio",
                    category = "Cultura",
                    tags = "Cultura, História, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1584810359583-96fc3448beaa?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "Keindahan Pulau Bali",
                    translationTitle = "A Beleza da Ilha de Bali",
                    content = "Bali adalah pulau wisata yang sangat terkenal di seluruh dunia. Bali memiliki pantai yang indah, seni budaya yang unik, dan alam yang asri.",
                    translationContent = "Bali é uma ilha turística muito famosa no mundo inteiro. Bali tem praias bonitas, arte cultural única e natureza exuberante.",
                    level = "N3",
                    language = "Indonésio",
                    category = "Estilo de Vida",
                    tags = "Viagem, Estilo de Vida, Cultura",
                    imageUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?q=80&w=600&auto=format&fit=crop"
                ),
                // 6. Inglês
                TextItem(
                    title = "The Grand Canyon",
                    translationTitle = "O Grand Canyon",
                    content = "The Grand Canyon is a very large canyon in Arizona, United States. It was carved by the Colorado River over millions of years. The views are incredible.",
                    translationContent = "O Grand Canyon é um desfiladeiro muito grande no Arizona, Estados Unidos. Foi esculpido pelo Rio Colorado ao longo de milhões de anos. As vistas são incríveis.",
                    level = "N4",
                    language = "Inglês",
                    category = "Cultura",
                    tags = "Ciências, Viagem, Geografia",
                    imageUrl = "https://images.unsplash.com/photo-1615551043360-33de8b5f410c?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "The Story of Tea",
                    translationTitle = "A História do Chá",
                    content = "Tea is one of the most popular drinks in the world. It started in ancient China and became very famous in England. People drink it warm.",
                    translationContent = "O chá é uma das bebidas mais populares do mundo. Começou na China antiga e tornou-se muito famoso na Inglaterra. As pessoas o bebem quente.",
                    level = "N3",
                    language = "Inglês",
                    category = "Estilo de Vida",
                    tags = "Culinária, Estilo de Vida, História",
                    imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?q=80&w=600&auto=format&fit=crop"
                ),
                // 7. Japonês
                TextItem(
                    title = "富士山と桜の美しさ",
                    translationTitle = "A Beleza do Monte Fuji e as Cerejeiras",
                    content = "富士山（ふじさん）は日本の一番高い山です。春になると、富士山のふもとで美しい桜の花が咲きます。この景色は日本のシンボルです。",
                    translationContent = "O Monte Fuji (Fujisan) é a montanha mais alta do Japão. Quando chega a primavera, lindas flores de cerejeira florescem na base do Monte Fuji. Esta vista é um símbolo do Japão.",
                    level = "N4",
                    language = "Japonês",
                    category = "Cultura",
                    tags = "Cultura, Viagem, Folclore",
                    imageUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "お寿司の歴史と文化",
                    translationTitle = "História e Cultura do Sushi",
                    content = "寿司（すし）は日本を代表する伝統的な料理です。新鮮な魚と酢飯を組み合わせて作ります。今では世界中で大人気のごちそうです。",
                    translationContent = "O sushi (sushi) é um prato tradicional representativo do Japão. É feito combinando peixe fresco e arroz temperado com vinagre. Hoje, é um banquete muito popular em todo o mundo.",
                    level = "N3",
                    language = "Japonês",
                    category = "Estilo de Vida",
                    tags = "Culinária, Estilo de Vida, Cultura",
                    imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?q=80&w=600&auto=format&fit=crop"
                ),
                // 8. Mandarim
                TextItem(
                    title = "万里长城的雄伟",
                    translationTitle = "A Majestade da Grande Muralha",
                    content = "万里长城是中国最著名的古代建筑。它像一条巨龙横跨在群山之间。长城有两千多年的历史，吸引着世界各地的游客。",
                    translationContent = "A Grande Muralha da China é a construção antiga mais famosa da China. Ela cruza as montanhas como um dragão gigante. Tem mais de dois mil anos de história e atrai turistas do mundo todo.",
                    level = "N4",
                    language = "Mandarim",
                    category = "Cultura",
                    tags = "Cultura, História, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1508193638397-1c4234db14d8?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "大熊猫的故乡",
                    translationTitle = "A Terra Natal do Urso Panda Gigante",
                    content = "大熊猫是中国的国宝，非常可爱。它们喜欢吃竹子，生活在四川省的高山森林里。许多大熊猫基地对游客开放。",
                    translationContent = "O panda gigante é o tesouro nacional da China e é extremamente fofo. Eles gostam de comer bambu e vivem nas florestas montanhosas da província de Sichuan. Muitas bases de pandas estão abertas para visitantes.",
                    level = "N3",
                    language = "Mandarim",
                    category = "Biologia",
                    tags = "Biologia, Ciências, Educação",
                    imageUrl = "https://images.unsplash.com/photo-1526318896980-cf78c088247c?q=80&w=600&auto=format&fit=crop"
                ),
                // 9. Português
                TextItem(
                    title = "O Cristo Redentor",
                    translationTitle = "Christ the Redeemer",
                    content = "O Cristo Redentor é uma estátua famosa localizada no topo do Morro do Corcovado, no Rio de Janeiro. É um dos maiores símbolos do Brasil e do mundo.",
                    translationContent = "O Cristo Redentor é uma estátua famosa localizada no topo do Morro do Corcovado, no Rio de Janeiro. É um dos maiores símbolos do Brasil e do mundo.",
                    level = "N4",
                    language = "Português",
                    category = "Cultura",
                    tags = "Cultura, Viagem, História",
                    imageUrl = "https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "A Floresta Amazônica",
                    translationTitle = "The Amazon Rainforest",
                    content = "A Floresta Amazônica é a maior floresta tropical do mundo. Ela abriga uma rica biodiversidade, com milhares de espécies de plantas e animais únicos.",
                    translationContent = "A Floresta Amazônica é a maior floresta tropical do mundo. Ela abriga uma rica biodiversidade, com milhares de espécies de plantas e animais únicos.",
                    level = "N3",
                    language = "Português",
                    category = "Biologia",
                    tags = "Biologia, Ciências, Educação",
                    imageUrl = "https://images.unsplash.com/photo-1516026672322-bc52d61a55d5?q=80&w=600&auto=format&fit=crop"
                ),
                // 10. Russo
                TextItem(
                    title = "Красная площадь",
                    translationTitle = "A Praça Vermelha",
                    content = "Красная площадь — это самое сердце Москвы. Здесь находится красивый храм Василия Блаженного и Кремль. Сюда приходят миллионы туристов.",
                    translationContent = "A Praça Vermelha é o coração de Moscou. Aqui fica o belo Templo de São Basílio e o Kremlin. Milhões de turistas visitam este local.",
                    level = "N4",
                    language = "Russo",
                    category = "Cultura",
                    tags = "Cultura, História, Viagem",
                    imageUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?q=80&w=600&auto=format&fit=crop"
                ),
                TextItem(
                    title = "Озеро Байкал",
                    translationTitle = "O Lago Baikal",
                    content = "Байкал — самое глубокое озеро на планете. В нём содержится очень чистая и пресная вода. Зимой озеро покрывается красивым прозрачным льдом.",
                    translationContent = "O Baikal é o lago mais profundo do planeta. Ele contém água doce extremamente pura. No inverno, o lago se cobre de um gelo transparente e lindo.",
                    level = "N3",
                    language = "Russo",
                    category = "Estilo de Vida",
                    tags = "Viagem, Estilo de Vida, Geografia",
                    imageUrl = "https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?q=80&w=600&auto=format&fit=crop"
                )
            )
            textDao.insertTexts(defaultTexts)
        }
    }
}
