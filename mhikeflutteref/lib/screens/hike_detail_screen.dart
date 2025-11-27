import 'package:flutter/material.dart';
import '../database/database_helper.dart';
import '../models/hike.dart';
import 'add_hike_screen.dart';
import 'observation_screen.dart';

class HikeDetailScreen extends StatelessWidget {
  final Hike hike;

  const HikeDetailScreen({super.key, required this.hike});

  Future<void> _deleteHike(BuildContext context) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Hike'),
        content: const Text(
          'Are you sure you want to delete this hike? All related observations will also be deleted.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirm == true) {
      await DatabaseHelper.instance.deleteHike(hike.id!);
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Hike deleted')),
        );
        Navigator.pop(context);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Hike Details'),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit),
            onPressed: () async {
              await Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => AddHikeScreen(hike: hike),
                ),
              );
              if (context.mounted) {
                Navigator.pop(context);
              }
            },
          ),
          IconButton(
            icon: const Icon(Icons.delete),
            onPressed: () => _deleteHike(context),
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Header Card
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    _getDifficultyColor(hike.difficulty),
                    _getDifficultyColor(hike.difficulty).withOpacity(0.7),
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    hike.name,
                    style: const TextStyle(
                      fontSize: 28,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.location_on, color: Colors.white, size: 20),
                      const SizedBox(width: 4),
                      Expanded(
                        child: Text(
                          hike.location,
                          style: const TextStyle(
                            fontSize: 16,
                            color: Colors.white,
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            // Details Card
            Padding(
              padding: const EdgeInsets.all(16),
              child: Card(
                elevation: 4,
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      _buildDetailRow(
                        Icons.calendar_today,
                        'Date',
                        hike.date,
                      ),
                      const Divider(height: 24),
                      _buildDetailRow(
                        Icons.straighten,
                        'Length',
                        '${hike.length} km',
                      ),
                      const Divider(height: 24),
                      _buildDetailRow(
                        Icons.trending_up,
                        'Difficulty',
                        hike.difficulty,
                      ),
                      const Divider(height: 24),
                      _buildDetailRow(
                        Icons.local_parking,
                        'Parking',
                        hike.parking ? 'Available' : 'Not Available',
                      ),
                      if (hike.description != null &&
                          hike.description!.isNotEmpty) ...[
                        const Divider(height: 24),
                        _buildDetailRow(
                          Icons.description,
                          'Description',
                          hike.description!,
                        ),
                      ],
                      if (hike.weather != null && hike.weather!.isNotEmpty) ...[
                        const Divider(height: 24),
                        _buildDetailRow(
                          Icons.wb_sunny,
                          'Weather',
                          hike.weather!,
                        ),
                      ],
                      if (hike.groupSize != null && hike.groupSize! > 0) ...[
                        const Divider(height: 24),
                        _buildDetailRow(
                          Icons.group,
                          'Group Size',
                          hike.groupSize.toString(),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ),

            // Observations Button
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ElevatedButton.icon(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => ObservationScreen(
                        hikeId: hike.id!,
                        hikeName: hike.name,
                      ),
                    ),
                  );
                },
                icon: const Icon(Icons.remove_red_eye),
                label: const Text('View Observations'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.all(16),
                  textStyle: const TextStyle(fontSize: 16),
                ),
              ),
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailRow(IconData icon, String label, String value) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, color: Colors.green),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: const TextStyle(
                  fontSize: 14,
                  color: Colors.grey,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                value,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Color _getDifficultyColor(String difficulty) {
    switch (difficulty.toLowerCase()) {
      case 'easy':
        return Colors.green;
      case 'medium':
        return Colors.orange;
      case 'hard':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }
}
