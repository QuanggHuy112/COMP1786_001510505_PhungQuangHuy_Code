import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/hike.dart';
import '../models/observation.dart';

class DatabaseHelper {
  static final DatabaseHelper instance = DatabaseHelper._init();
  static Database? _database;

  DatabaseHelper._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('mhike.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);

    return await openDatabase(
      path,
      version: 1,
      onCreate: _createDB,
    );
  }

  Future _createDB(Database db, int version) async {
    const idType = 'INTEGER PRIMARY KEY AUTOINCREMENT';
    const textType = 'TEXT NOT NULL';
    const intType = 'INTEGER NOT NULL';
    const realType = 'REAL NOT NULL';

    await db.execute('''
      CREATE TABLE hikes (
        id $idType,
        name $textType,
        location $textType,
        date $textType,
        parking $intType,
        length $realType,
        difficulty $textType,
        description TEXT,
        weather TEXT,
        groupSize INTEGER
      )
    ''');

    await db.execute('''
      CREATE TABLE observations (
        id $idType,
        hikeId $intType,
        observationText $textType,
        time $textType,
        comments TEXT,
        FOREIGN KEY (hikeId) REFERENCES hikes (id) ON DELETE CASCADE
      )
    ''');
  }

  // Hike CRUD operations
  Future<int> insertHike(Hike hike) async {
    final db = await database;
    return await db.insert('hikes', hike.toMap());
  }

  Future<List<Hike>> getAllHikes() async {
    final db = await database;
    final result = await db.query('hikes', orderBy: 'id DESC');
    return result.map((map) => Hike.fromMap(map)).toList();
  }

  Future<Hike?> getHike(int id) async {
    final db = await database;
    final maps = await db.query(
      'hikes',
      where: 'id = ?',
      whereArgs: [id],
    );

    if (maps.isNotEmpty) {
      return Hike.fromMap(maps.first);
    }
    return null;
  }

  Future<int> updateHike(Hike hike) async {
    final db = await database;
    return await db.update(
      'hikes',
      hike.toMap(),
      where: 'id = ?',
      whereArgs: [hike.id],
    );
  }

  Future<int> deleteHike(int id) async {
    final db = await database;

    // Delete related observations first
    await db.delete(
      'observations',
      where: 'hikeId = ?',
      whereArgs: [id],
    );

    // Then delete the hike
    return await db.delete(
      'hikes',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<int> deleteAllHikes() async {
    final db = await database;
    await db.delete('observations');
    return await db.delete('hikes');
  }

  Future<List<Hike>> searchHikesByName(String keyword) async {
    final db = await database;
    final result = await db.query(
      'hikes',
      where: 'name LIKE ?',
      whereArgs: ['%$keyword%'],
      orderBy: 'id DESC',
    );
    return result.map((map) => Hike.fromMap(map)).toList();
  }

  Future<List<Hike>> advancedSearch({
    String? name,
    String? location,
    String? distance,
    String? date,
  }) async {
    final db = await database;
    final List<String> whereConditions = [];
    final List<dynamic> whereArgs = [];

    if (name != null && name.isNotEmpty) {
      whereConditions.add('name LIKE ?');
      whereArgs.add('%$name%');
    }

    if (location != null && location.isNotEmpty) {
      whereConditions.add('location LIKE ?');
      whereArgs.add('%$location%');
    }

    if (distance != null && distance.isNotEmpty) {
      try {
        final distValue = double.parse(distance);
        whereConditions.add('length >= ?');
        whereArgs.add(distValue);
      } catch (e) {
        // Invalid distance format, skip this condition
      }
    }

    if (date != null && date.isNotEmpty) {
      whereConditions.add('date LIKE ?');
      whereArgs.add('%$date%');
    }

    if (whereConditions.isEmpty) {
      return getAllHikes();
    }

    final result = await db.query(
      'hikes',
      where: whereConditions.join(' AND '),
      whereArgs: whereArgs,
      orderBy: 'id DESC',
    );

    return result.map((map) => Hike.fromMap(map)).toList();
  }

  // Observation CRUD operations
  Future<int> insertObservation(Observation observation) async {
    final db = await database;
    return await db.insert('observations', observation.toMap());
  }

  Future<List<Observation>> getObservationsByHike(int hikeId) async {
    final db = await database;
    final result = await db.query(
      'observations',
      where: 'hikeId = ?',
      whereArgs: [hikeId],
      orderBy: 'id DESC',
    );
    return result.map((map) => Observation.fromMap(map)).toList();
  }

  Future<int> updateObservation(Observation observation) async {
    final db = await database;
    return await db.update(
      'observations',
      observation.toMap(),
      where: 'id = ?',
      whereArgs: [observation.id],
    );
  }

  Future<int> deleteObservation(int id) async {
    final db = await database;
    return await db.delete(
      'observations',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future close() async {
    final db = await database;
    db.close();
  }
}
