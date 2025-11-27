class Hike {
  int? id;
  String name;
  String location;
  String date;
  bool parking;
  double length;
  String difficulty;
  String? description;
  String? weather;
  int? groupSize;

  Hike({
    this.id,
    required this.name,
    required this.location,
    required this.date,
    required this.parking,
    required this.length,
    required this.difficulty,
    this.description,
    this.weather,
    this.groupSize,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'location': location,
      'date': date,
      'parking': parking ? 1 : 0,
      'length': length,
      'difficulty': difficulty,
      'description': description,
      'weather': weather,
      'groupSize': groupSize,
    };
  }

  factory Hike.fromMap(Map<String, dynamic> map) {
    return Hike(
      id: map['id'],
      name: map['name'],
      location: map['location'],
      date: map['date'],
      parking: map['parking'] == 1,
      length: map['length'],
      difficulty: map['difficulty'],
      description: map['description'],
      weather: map['weather'],
      groupSize: map['groupSize'],
    );
  }

  Hike copyWith({
    int? id,
    String? name,
    String? location,
    String? date,
    bool? parking,
    double? length,
    String? difficulty,
    String? description,
    String? weather,
    int? groupSize,
  }) {
    return Hike(
      id: id ?? this.id,
      name: name ?? this.name,
      location: location ?? this.location,
      date: date ?? this.date,
      parking: parking ?? this.parking,
      length: length ?? this.length,
      difficulty: difficulty ?? this.difficulty,
      description: description ?? this.description,
      weather: weather ?? this.weather,
      groupSize: groupSize ?? this.groupSize,
    );
  }
}
