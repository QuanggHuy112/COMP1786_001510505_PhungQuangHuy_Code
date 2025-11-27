import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../database/database_helper.dart';
import '../models/observation.dart';

class ObservationScreen extends StatefulWidget {
  final int hikeId;
  final String hikeName;

  const ObservationScreen({
    super.key,
    required this.hikeId,
    required this.hikeName,
  });

  @override
  State<ObservationScreen> createState() => _ObservationScreenState();
}

class _ObservationScreenState extends State<ObservationScreen> {
  final DatabaseHelper _dbHelper = DatabaseHelper.instance;
  List<Observation> _observations = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadObservations();
  }

  Future<void> _loadObservations() async {
    setState(() => _isLoading = true);
    final observations = await _dbHelper.getObservationsByHike(widget.hikeId);
    setState(() {
      _observations = observations;
      _isLoading = false;
    });
  }

  Future<void> _showAddDialog() async {
    final textController = TextEditingController();
    final commentsController = TextEditingController();
    DateTime selectedDateTime = DateTime.now();
    String timeText = DateFormat('dd/MM/yyyy HH:mm').format(selectedDateTime);

    await showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: const Text('Add Observation'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    TextField(
                      controller: textController,
                      decoration: const InputDecoration(
                        labelText: 'Observation *',
                        border: OutlineInputBorder(),
                      ),
                      maxLines: 3,
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      decoration: InputDecoration(
                        labelText: 'Time',
                        border: const OutlineInputBorder(),
                        hintText: timeText,
                      ),
                      readOnly: true,
                      onTap: () async {
                        final date = await showDatePicker(
                          context: context,
                          initialDate: selectedDateTime,
                          firstDate: DateTime(2000),
                          lastDate: DateTime(2100),
                        );

                        if (date != null) {
                          if (context.mounted) {
                            final time = await showTimePicker(
                              context: context,
                              initialTime: TimeOfDay.fromDateTime(selectedDateTime),
                            );

                            if (time != null) {
                              selectedDateTime = DateTime(
                                date.year,
                                date.month,
                                date.day,
                                time.hour,
                                time.minute,
                              );
                              setDialogState(() {
                                timeText = DateFormat('dd/MM/yyyy HH:mm')
                                    .format(selectedDateTime);
                              });
                            }
                          }
                        }
                      },
                      controller: TextEditingController(text: timeText),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: commentsController,
                      decoration: const InputDecoration(
                        labelText: 'Comments (Optional)',
                        border: OutlineInputBorder(),
                      ),
                      maxLines: 2,
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Cancel'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    final text = textController.text.trim();
                    if (text.isEmpty) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Observation text is required'),
                        ),
                      );
                      return;
                    }

                    final observation = Observation(
                      hikeId: widget.hikeId,
                      observationText: text,
                      time: timeText,
                      comments: commentsController.text.trim().isEmpty
                          ? null
                          : commentsController.text.trim(),
                    );

                    await _dbHelper.insertObservation(observation);
                    if (context.mounted) {
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Observation added')),
                      );
                      _loadObservations();
                    }
                  },
                  child: const Text('Add'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  Future<void> _showOptionsDialog(Observation obs) async {
    await showDialog(
      context: context,
      builder: (context) => SimpleDialog(
        title: const Text('Choose Action'),
        children: [
          SimpleDialogOption(
            onPressed: () {
              Navigator.pop(context);
              _showDetailDialog(obs);
            },
            child: const Row(
              children: [
                Icon(Icons.info),
                SizedBox(width: 8),
                Text('View Details'),
              ],
            ),
          ),
          SimpleDialogOption(
            onPressed: () {
              Navigator.pop(context);
              _showEditDialog(obs);
            },
            child: const Row(
              children: [
                Icon(Icons.edit),
                SizedBox(width: 8),
                Text('Edit'),
              ],
            ),
          ),
          SimpleDialogOption(
            onPressed: () {
              Navigator.pop(context);
              _confirmDelete(obs);
            },
            child: const Row(
              children: [
                Icon(Icons.delete, color: Colors.red),
                SizedBox(width: 8),
                Text('Delete', style: TextStyle(color: Colors.red)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _showDetailDialog(Observation obs) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Observation Details'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Observation:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            Text(obs.observationText),
            const SizedBox(height: 16),
            const Text(
              'Time:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            Text(obs.time),
            const SizedBox(height: 16),
            const Text(
              'Comments:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            Text(obs.comments ?? 'None'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  Future<void> _showEditDialog(Observation obs) async {
    final textController = TextEditingController(text: obs.observationText);
    final commentsController = TextEditingController(text: obs.comments ?? '');
    String timeText = obs.time;
    DateTime selectedDateTime = DateTime.now();

    try {
      selectedDateTime = DateFormat('dd/MM/yyyy HH:mm').parse(obs.time);
    } catch (e) {
      // Keep current time if parsing fails
    }

    await showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: const Text('Edit Observation'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    TextField(
                      controller: textController,
                      decoration: const InputDecoration(
                        labelText: 'Observation *',
                        border: OutlineInputBorder(),
                      ),
                      maxLines: 3,
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      decoration: InputDecoration(
                        labelText: 'Time',
                        border: const OutlineInputBorder(),
                        hintText: timeText,
                      ),
                      readOnly: true,
                      onTap: () async {
                        final date = await showDatePicker(
                          context: context,
                          initialDate: selectedDateTime,
                          firstDate: DateTime(2000),
                          lastDate: DateTime(2100),
                        );

                        if (date != null) {
                          if (context.mounted) {
                            final time = await showTimePicker(
                              context: context,
                              initialTime: TimeOfDay.fromDateTime(selectedDateTime),
                            );

                            if (time != null) {
                              selectedDateTime = DateTime(
                                date.year,
                                date.month,
                                date.day,
                                time.hour,
                                time.minute,
                              );
                              setDialogState(() {
                                timeText = DateFormat('dd/MM/yyyy HH:mm')
                                    .format(selectedDateTime);
                              });
                            }
                          }
                        }
                      },
                      controller: TextEditingController(text: timeText),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: commentsController,
                      decoration: const InputDecoration(
                        labelText: 'Comments (Optional)',
                        border: OutlineInputBorder(),
                      ),
                      maxLines: 2,
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Cancel'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    final text = textController.text.trim();
                    if (text.isEmpty) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Observation text is required'),
                        ),
                      );
                      return;
                    }

                    final updatedObs = Observation(
                      id: obs.id,
                      hikeId: obs.hikeId,
                      observationText: text,
                      time: timeText,
                      comments: commentsController.text.trim().isEmpty
                          ? null
                          : commentsController.text.trim(),
                    );

                    await _dbHelper.updateObservation(updatedObs);
                    if (context.mounted) {
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Observation updated')),
                      );
                      _loadObservations();
                    }
                  },
                  child: const Text('Update'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  Future<void> _confirmDelete(Observation obs) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Observation'),
        content: const Text('Are you sure you want to delete this observation?'),
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
      await _dbHelper.deleteObservation(obs.id!);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Observation deleted')),
        );
        _loadObservations();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Observations - ${widget.hikeName}'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _observations.isEmpty
          ? Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.remove_red_eye_outlined,
              size: 80,
              color: Colors.grey[400],
            ),
            const SizedBox(height: 16),
            Text(
              'No observations yet',
              style: TextStyle(
                fontSize: 18,
                color: Colors.grey[600],
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Tap + to add your first observation',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[500],
              ),
            ),
          ],
        ),
      )
          : ListView.builder(
        itemCount: _observations.length,
        padding: const EdgeInsets.all(8),
        itemBuilder: (context, index) {
          final obs = _observations[index];
          return Card(
            elevation: 2,
            margin: const EdgeInsets.symmetric(
              horizontal: 8,
              vertical: 4,
            ),
            child: ListTile(
              contentPadding: const EdgeInsets.all(12),
              leading: const CircleAvatar(
                child: Icon(Icons.visibility),
              ),
              title: Text(
                obs.observationText,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      const Icon(
                        Icons.access_time,
                        size: 16,
                        color: Colors.grey,
                      ),
                      const SizedBox(width: 4),
                      Text(obs.time),
                    ],
                  ),
                ],
              ),
              trailing: const Icon(Icons.more_vert),
              onTap: () => _showOptionsDialog(obs),
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddDialog,
        child: const Icon(Icons.add),
      ),
    );
  }
}
