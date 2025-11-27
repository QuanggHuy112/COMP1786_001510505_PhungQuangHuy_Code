import 'package:flutter/material.dart';
import '../database/database_helper.dart';
import '../models/hike.dart';

class ConfirmationScreen extends StatelessWidget {
  final Hike hike;
  final bool isEdit;

  const ConfirmationScreen({
    super.key,
    required this.hike,
    required this.isEdit,
  });

  Future<void> _saveHike(BuildContext context) async {
    try {
      final dbHelper = DatabaseHelper.instance;

      if (isEdit) {
        await dbHelper.updateHike(hike);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Hike updated successfully')),
          );
        }
      } else {
        await dbHelper.insertHike(hike);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Hike saved successfully')),
          );
        }
      }

      if (context.mounted) {
        Navigator.of(context).popUntil((route) => route.isFirst);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${e.toString()}')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Confirm Details'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildInfoRow('Name', hike.name, Icons.title),
                    const Divider(height: 24),
                    _buildInfoRow('Location', hike.location, Icons.location_on),
                    const Divider(height: 24),
                    _buildInfoRow('Date', hike.date, Icons.calendar_today),
                    const Divider(height: 24),
                    _buildInfoRow(
                      'Parking',
                      hike.parking ? 'Yes' : 'No',
                      Icons.local_parking,
                    ),
                    const Divider(height: 24),
                    _buildInfoRow(
                      'Length',
                      '${hike.length} km',
                      Icons.straighten,
                    ),
                    const Divider(height: 24),
                    _buildInfoRow(
                      'Difficulty',
                      hike.difficulty,
                      Icons.trending_up,
                    ),
                    if (hike.description != null &&
                        hike.description!.isNotEmpty) ...[
                      const Divider(height: 24),
                      _buildInfoRow(
                        'Description',
                        hike.description!,
                        Icons.description,
                      ),
                    ],
                    if (hike.weather != null && hike.weather!.isNotEmpty) ...[
                      const Divider(height: 24),
                      _buildInfoRow(
                        'Weather Expectation',
                        hike.weather!,
                        Icons.wb_sunny,
                      ),
                    ],
                    if (hike.groupSize != null && hike.groupSize! > 0) ...[
                      const Divider(height: 24),
                      _buildInfoRow(
                        'Group Size',
                        hike.groupSize.toString(),
                        Icons.group,
                      ),
                    ],
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: () => _saveHike(context),
              icon: const Icon(Icons.save),
              label: Text(isEdit ? 'Update Hike' : 'Save Hike'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                textStyle: const TextStyle(fontSize: 18),
                backgroundColor: Colors.green,
                foregroundColor: Colors.white,
              ),
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: () => Navigator.pop(context),
              icon: const Icon(Icons.edit),
              label: const Text('Edit'),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                textStyle: const TextStyle(fontSize: 18),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value, IconData icon) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 24, color: Colors.green),
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
}
