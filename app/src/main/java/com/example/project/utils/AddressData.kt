package com.example.project.utils


object AddressData {
    val provinces = listOf(
        "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cần Thơ", "Cao Bằng", "Đà Nẵng", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Nội", "Hà Tĩnh", "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "TP Hồ Chí Minh", "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    )

    val districts = mapOf(
        "TP Hồ Chí Minh" to listOf("Quận 1", "Quận 3", "Quận 4", "Quận 5", "Quận 6", "Quận 7", "Quận 8", "Quận 10", "Quận 11", "Quận 12", "Quận Bình Tân", "Quận Bình Thạnh", "Quận Gò Vấp", "Quận Phú Nhuận", "Quận Tân Bình", "Quận Tân Phú", "Huyện Bình Chánh", "Huyện Cần Giờ", "Huyện Củ Chi", "Huyện Hóc Môn", "Huyện Nhà Bè", "Thành phố Thủ Đức"),
        "Hà Nội" to listOf("Quận Ba Đình", "Quận Bắc Từ Liêm", "Quận Cầu Giấy", "Quận Đống Đa", "Quận Hà Đông", "Quận Hai Bà Trưng", "Quận Hoàn Kiếm", "Quận Hoàng Mai", "Quận Long Biên", "Quận Nam Từ Liêm", "Quận Tây Hồ", "Quận Thanh Xuân", "Thị xã Sơn Tây", "Huyện Ba Vì", "Huyện Chương Mỹ", "Huyện Đan Phượng", "Huyện Đông Anh", "Huyện Gia Lâm", "Huyện Hoài Đức", "Huyện Mê Linh", "Huyện Mỹ Đức", "Huyện Phú Xuyên", "Huyện Phúc Thọ", "Huyện Quốc Oai", "Huyện Thạch Thất", "Huyện Thanh Oai", "Huyện Thanh Trì", "Huyện Thường Tín", "Huyện Ứng Hòa"),
        "Đà Nẵng" to listOf("Quận Cẩm Lệ", "Quận Hải Châu", "Quận Liên Chiểu", "Quận Ngũ Hành Sơn", "Quận Sơn Trà", "Quận Thanh Khê", "Huyện Hòa Vang", "Huyện Hoàng Sa"),
        "Cần Thơ" to listOf("Quận Bình Thủy", "Quận Cái Răng", "Quận Ninh Kiều", "Quận Ô Môn", "Quận Thốt Nốt", "Huyện Cờ Đỏ", "Huyện Phong Điền", "Huyện Thới Lai", "Huyện Vĩnh Thạnh"),
        "Hải Phòng" to listOf("Quận Dương Kinh", "Quận Đồ Sơn", "Quận Hải An", "Quận Hồng Bàng", "Quận Kiến An", "Quận Lê Chân", "Quận Ngô Quyền", "Huyện An Dương", "Huyện An Lão", "Huyện Bạch Long Vĩ", "Huyện Cát Hải", "Huyện Kiến Thụy", "Huyện Thủy Nguyên", "Huyện Tiên Lãng", "Huyện Vĩnh Bảo"),
        "An Giang" to listOf("Thành phố Long Xuyên", "Thành phố Châu Đốc", "Thị xã Tân Châu", "Thị xã Tịnh Biên", "Huyện An Phú", "Huyện Châu Phú", "Huyện Châu Thành", "Huyện Chợ Mới", "Huyện Phú Tân", "Huyện Thoại Sơn", "Huyện Tri Tôn"),
        "Lâm Đồng" to listOf("Thành phố Đà Lạt", "Thành phố Bảo Lộc", "Huyện Bảo Lâm", "Huyện Cát Tiên", "Huyện Di Linh", "Huyện Đạ Huoai", "Huyện Đạ Tẻh", "Huyện Đam Rông", "Huyện Đơn Dương", "Huyện Đức Trọng", "Huyện Lạc Dương", "Huyện Lâm Hà"),
        "Bình Dương" to listOf("Thành phố Thủ Dầu Một", "Thành phố Thuận An", "Thành phố Dĩ An", "Thành phố Tân Uyên", "Thành phố Bến Cát", "Huyện Bắc Tân Uyên", "Huyện Dầu Tiếng", "Huyện Phú Giáo", "Huyện Bàu Bàng")
    )

    private val wardsDetail = mapOf(
        "Quận 1" to listOf("Phường Bến Nghé", "Phường Bến Thành", "Phường Cô Giang", "Phường Đa Kao", "Phường Nguyễn Cư Trinh", "Phường Nguyễn Thái Bình", "Phường Phạm Ngũ Lão", "Phường Tân Định"),
        "Quận Hoàn Kiếm" to listOf("Phường Cửa Đông", "Phường Cửa Nam", "Phường Đồng Xuân", "Phường Hàng Bạc", "Phường Hàng Bài", "Phường Hàng Bồ", "Phường Hàng Bông", "Phường Hàng Buồm", "Phường Hàng Đào", "Phường Hàng Gai", "Phường Hàng Mã", "Phường Hàng Trống", "Phường Lý Thái Tổ", "Phường Phan Chu Trinh", "Phường Phúc Tân", "Phường Trần Hưng Đạo", "Phường Tràng Tiền"),
        "Thành phố Đà Lạt" to listOf("Phường 1", "Phường 2", "Phường 3", "Phường 4", "Phường 5", "Phường 6", "Phường 7", "Phường 8", "Phường 9", "Phường 10", "Phường 11", "Phường 12", "Xã Tà Nung", "Xã Trạm Hành", "Xã Xuân Thọ", "Xã Xuân Trường")
    )


    //tự động sinh ra danh sách mẫu (Phường 1 -> Phường 15)
    fun getWardsForDistrict(district: String): List<String> {
        return wardsDetail[district] ?: (1..15).map { "Phường/Xã $it" }
    }
}
