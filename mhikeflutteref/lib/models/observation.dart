class Observation {
  int? id;
  int hikeId;
  String observationText;
  String time;
  String? comments;

  Observation({
    this.id,
    required this.hikeId,
    required this.observationText,
    required this.time,
    this.comments,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'hikeId': hikeId,
      'observationText': observationText,
      'time': time,
      'comments': comments,
    };
  }

  factory Observation.fromMap(Map<String, dynamic> map) {
    return Observation(
      id: map['id'],
      hikeId: map['hikeId'],
      observationText: map['observationText'],
      time: map['time'],
      comments: map['comments'],
    );
  }
}
